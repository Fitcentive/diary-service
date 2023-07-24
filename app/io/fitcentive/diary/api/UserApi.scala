package io.fitcentive.diary.api

import io.fitcentive.diary.domain.user.FitnessUserProfile
import io.fitcentive.diary.repositories.{ExerciseDiaryRepository, FoodDiaryRepository, UserRepository}
import io.fitcentive.sdk.error.{DomainError, EntityNotFoundError}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserApi @Inject() (
  userRepository: UserRepository,
  exerciseDiaryRepository: ExerciseDiaryRepository,
  foodDiaryRepository: FoodDiaryRepository
)(implicit ec: ExecutionContext) {

  def getUserFitnessProfile(userId: UUID): Future[Either[DomainError, FitnessUserProfile]] =
    userRepository
      .getFitnessUserProfile(userId)
      .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("No user fitness profile found!"))))

  def upsertUserFitnessProfile(
    userId: UUID,
    update: FitnessUserProfile.Update
  ): Future[Either[DomainError, FitnessUserProfile]] =
    userRepository
      .upsertFitnessUserProfile(userId, update)
      .map(Right.apply)

  def deleteUserDiaryData(userId: UUID): Future[Unit] =
    for {
      _ <- userRepository.deleteFitnessUserProfile(userId)
      _ <- exerciseDiaryRepository.deleteAllCardioWorkoutsForUser(userId)
      _ <- exerciseDiaryRepository.deleteAllStrengthWorkoutsForUser(userId)
      _ <- foodDiaryRepository.deleteAllFoodEntriesForUser(userId)
      _ <- exerciseDiaryRepository.deleteAllUserStepsData(userId)
    } yield ()
}
