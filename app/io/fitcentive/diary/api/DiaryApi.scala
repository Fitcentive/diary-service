package io.fitcentive.diary.api

import io.fitcentive.diary.domain.exercise.{CardioWorkout, StrengthWorkout}
import io.fitcentive.diary.domain.food.FoodEntry
import io.fitcentive.diary.repositories.{ExerciseDiaryRepository, FoodDiaryRepository}

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DiaryApi @Inject() (exerciseDiaryRepository: ExerciseDiaryRepository, foodDiaryRepository: FoodDiaryRepository)(
  implicit ec: ExecutionContext
) {

  // --------------------------------
  // Exercise Diary API methods
  // --------------------------------
  def insertCardioDiaryEntry(userId: UUID, create: CardioWorkout.Create): Future[CardioWorkout] =
    exerciseDiaryRepository
      .insertCardioWorkoutForUser(id = UUID.randomUUID(), userId = userId, create = create)

  def deleteCardioDiaryEntry(userId: UUID, cardioWorkoutEntryId: UUID): Future[Unit] =
    exerciseDiaryRepository
      .deleteCardioWorkoutForUser(userId, cardioWorkoutEntryId)

  def insertStrengthDiaryEntry(userId: UUID, create: StrengthWorkout.Create): Future[StrengthWorkout] =
    exerciseDiaryRepository
      .insertStrengthWorkoutForUser(id = UUID.randomUUID(), userId = userId, create = create)

  def deleteStrengthDiaryEntry(userId: UUID, strengthWorkoutEntryId: UUID): Future[Unit] =
    exerciseDiaryRepository
      .deleteStrengthWorkoutForUser(userId, strengthWorkoutEntryId)

  def getCardioEntriesForUserByDay(userId: UUID, day: Instant): Future[Seq[CardioWorkout]] =
    exerciseDiaryRepository
      .getAllCardioWorkoutsForDayByUser(userId = userId, day = day)

  def getStrengthEntriesForUserByDay(userId: UUID, day: Instant): Future[Seq[StrengthWorkout]] =
    exerciseDiaryRepository
      .getAllStrengthWorkoutsForDayByUser(userId = userId, day = day)

  def getUserMostRecentlyViewedWorkoutIds(userId: UUID): Future[Seq[UUID]] =
    exerciseDiaryRepository
      .getUserRecentlyViewedWorkoutIds(userId)

  def addUserMostRecentlyViewedWorkout(userId: UUID, workoutId: UUID): Future[Unit] =
    for {
      existingHistory <- exerciseDiaryRepository.getUserRecentlyViewedWorkoutIds(userId)
      _ <-
        if (existingHistory.length >= 10) {
          existingHistory.lastOption
            .map(
              staleWorkoutId => exerciseDiaryRepository.deleteMostRecentlyViewedWorkoutForUser(userId, staleWorkoutId)
            )
            .getOrElse(Future.unit)
        } else {
          Future.unit
        }
      _ <- exerciseDiaryRepository.upsertMostRecentlyViewedWorkoutForUser(userId, workoutId)
    } yield ()

  // --------------------------------
  // Food Diary API methods
  // --------------------------------
  def insertFoodDiaryEntry(userId: UUID, create: FoodEntry.Create): Future[FoodEntry] =
    foodDiaryRepository
      .insertFoodDiaryEntry(id = UUID.randomUUID(), userId = userId, create = create)

  def deleteFoodDiaryEntry(userId: UUID, foodEntryId: UUID): Future[Unit] =
    foodDiaryRepository
      .deleteFoodDiaryEntry(userId, foodEntryId)

  def getFoodEntriesForUserByDay(userId: UUID, day: Instant): Future[Seq[FoodEntry]] =
    foodDiaryRepository
      .getAllFoodEntriesForDayByUser(userId, day)

  def getUserMostRecentlyViewedFoodIds(userId: UUID): Future[Seq[Int]] =
    foodDiaryRepository
      .getUserRecentlyViewedFoodIds(userId)

  def addUserMostRecentlyViewedFood(userId: UUID, foodId: Int): Future[Unit] =
    for {
      existingHistory <- foodDiaryRepository.getUserRecentlyViewedFoodIds(userId)
      _ <-
        if (existingHistory.length >= 10) {
          existingHistory.lastOption
            .map(staleFoodId => foodDiaryRepository.deleteMostRecentlyViewedFoodForUser(userId, staleFoodId))
            .getOrElse(Future.unit)
        } else {
          Future.unit
        }
      _ <- foodDiaryRepository.upsertMostRecentlyViewedFoodForUser(userId, foodId)
    } yield ()

}
