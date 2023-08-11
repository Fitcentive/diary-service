package io.fitcentive.diary.api

import cats.data.EitherT
import io.fitcentive.sdk.error.{DomainError, EntityNotFoundError}
import io.fitcentive.diary.domain.wger.ExerciseDefinition
import io.fitcentive.diary.repositories.{ExerciseDiaryRepository, FoodDiaryRepository, UserRepository}
import io.fitcentive.diary.services._

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class ExerciseApi @Inject() (
  exerciseService: ExerciseService,
  userRepository: UserRepository,
  exerciseDiaryRepository: ExerciseDiaryRepository,
  foodDiaryRepository: FoodDiaryRepository,
  messageBusService: MessageBusService,
)(implicit ec: ExecutionContext) {

  val ENGLISH = 2

  val defaultLimit = 50
  val defaultOffset = 0

  val ESToffsetInMinutes = 240;

  def getExerciseInfoForWorkoutId(workoutId: UUID): Future[Either[DomainError, ExerciseDefinition]] =
    (for {
      exerciseDefinitionOpt <-
        EitherT[Future, DomainError, Option[ExerciseDefinition]](exerciseService.getExerciseInfoForWorkoutId(workoutId))
      exerciseDefinition <- EitherT[Future, DomainError, ExerciseDefinition] {
        exerciseDefinitionOpt
          .map(e => Future.successful(Right.apply(e)))
          .getOrElse(
            Future.successful(Left(EntityNotFoundError(s"No exercise definition found for workoutId: $workoutId")))
          )
      }
    } yield exerciseDefinition).value

  // Wrap in future to ACK pubsub message immediately
  // todo - currently, we do for EST by harcoding offset. Ideally, we want to consume user data based on their timezone offset
  def checkIfUsersNeedPromptToLogDiaryEntriesEvent(userIds: Seq[UUID]): Future[Unit] =
    Future {
      val windowStart = DateTimeFormatter
        .ofPattern("yyyy-MM-dd")
        .format(LocalDateTime.ofInstant(Instant.now, ZoneOffset.UTC))
        .pipe(d => LocalDate.parse(d).atStartOfDay().toInstant(ZoneOffset.UTC))
        .plus(-ESToffsetInMinutes, ChronoUnit.MINUTES)
      val windowEnd = windowStart.plus(1, ChronoUnit.DAYS)

      userIds.map { currentUserId =>
        (for {
          foodDiaryEntriesCount <-
            foodDiaryRepository.getCountOfFoodEntriesForDayByUser(currentUserId, windowStart, windowEnd)
          cardioDiaryEntryCount <-
            exerciseDiaryRepository.getCountOfCardioWorkoutsForDayByUser(currentUserId, windowStart, windowEnd)
          strengthDiaryEntryCount <-
            exerciseDiaryRepository.getCountOfStrengthWorkoutsForDayByUser(currentUserId, windowStart, windowEnd)
        } yield foodDiaryEntriesCount + cardioDiaryEntryCount + strengthDiaryEntryCount)
          .map { count =>
            if (count == 0)
              messageBusService.publishNotifyUserToPromptForDiaryEntry(currentUserId)
          }
      }
    }

  // Wrap in future to ACK pubsub message immediately
  def checkIfUsersNeedPromptToLogWeightEvent(userIds: Seq[UUID]): Future[Unit] =
    Future {
      userIds.map { currentUserId =>
        userRepository
          .getFitnessUserProfile(currentUserId)
          .map {
            case Some(profile) =>
              val lastUpdatedAtDate = DateTimeFormatter
                .ofPattern("yyyy-MM-dd")
                .format(LocalDateTime.ofInstant(profile.updatedAt, ZoneOffset.UTC))
              val todaysDate = DateTimeFormatter
                .ofPattern("yyyy-MM-dd")
                .format(LocalDateTime.ofInstant(Instant.now, ZoneOffset.UTC))

              if (lastUpdatedAtDate != todaysDate)
                messageBusService.publishNotifyUserToPromptForWeightEntry(currentUserId)

            case None => ()
          }
      }
    }

  def getAllExerciseInfo: Future[Either[DomainError, Seq[ExerciseDefinition]]] =
    exerciseService.getCompleteExerciseDetailedInfo
      .map(_.map(_.filter(_.language.id == ENGLISH)))

  implicit class OptionalStringToEmpty(s: Option[String]) {
    def optString: String =
      s.fold("")(identity)
  }

}
