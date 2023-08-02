package io.fitcentive.diary.api

import io.fitcentive.diary.domain.user.FitnessUserProfile
import io.fitcentive.diary.repositories.{ExerciseDiaryRepository, FoodDiaryRepository, UserRepository}
import io.fitcentive.diary.services.MessageBusService
import io.fitcentive.sdk.error.{DomainError, EntityNotFoundError}

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserApi @Inject() (
  userRepository: UserRepository,
  exerciseDiaryRepository: ExerciseDiaryRepository,
  foodDiaryRepository: FoodDiaryRepository,
  messageBusService: MessageBusService,
)(implicit ec: ExecutionContext) {

  def getUserFitnessProfile(userId: UUID): Future[Either[DomainError, FitnessUserProfile]] =
    userRepository
      .getFitnessUserProfile(userId)
      .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("No user fitness profile found!"))))

  def upsertUserFitnessProfile(userId: UUID, update: FitnessUserProfile.Update): Future[FitnessUserProfile] =
    for {
      updatedProfile <- userRepository.upsertFitnessUserProfile(userId, update)
      entryDate = LocalDateTime.ofInstant(Instant.now, ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE)
      _ <- messageBusService.publishUserWeightUpdatedEvent(userId, entryDate, updatedProfile.weightInLbs)
    } yield updatedProfile

  def deleteUserDiaryData(userId: UUID): Future[Unit] =
    for {
      _ <- userRepository.deleteFitnessUserProfile(userId)
      _ <- exerciseDiaryRepository.deleteAllCardioWorkoutsForUser(userId)
      _ <- exerciseDiaryRepository.deleteAllStrengthWorkoutsForUser(userId)
      _ <- foodDiaryRepository.deleteAllFoodEntriesForUser(userId)
      _ <- exerciseDiaryRepository.deleteAllUserStepsData(userId)
    } yield ()
}
