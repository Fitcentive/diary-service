package io.fitcentive.diary.api

import cats.data.EitherT
import io.fitcentive.sdk.error.{DomainError, EntityNotFoundError}
import io.fitcentive.diary.domain.wger.ExerciseDefinition
import io.fitcentive.diary.repositories.UserRepository
import io.fitcentive.diary.services._

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExerciseApi @Inject() (
  exerciseService: ExerciseService,
  userRepository: UserRepository,
  messageBusService: MessageBusService,
)(implicit ec: ExecutionContext) {

  val ENGLISH = 2

  val defaultLimit = 50
  val defaultOffset = 0

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
  def checkIfUsersNeedPromptToLogWeightEvent(userIds: Seq[UUID]): Future[Unit] =
    Future {
      println(s"checkIfUsersNeedPromptToLogWeightEvent is called for userIds $userIds")
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
