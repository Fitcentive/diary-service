package io.fitcentive.diary.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.diary.domain.exercise
import io.fitcentive.diary.domain.exercise.{CardioWorkout, StrengthWorkout}
import io.fitcentive.diary.infrastructure.database.sql.AnormExerciseDiaryRepository

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormExerciseDiaryRepository])
trait ExerciseDiaryRepository {
  def getCardioWorkoutsById(cardioDiaryEntryIds: Seq[UUID]): Future[Seq[CardioWorkout]]
  def getCardioWorkoutByIdForUser(userId: UUID, cardioDiaryEntryId: UUID): Future[Option[CardioWorkout]]
  def updateCardioWorkoutByIdForUser(
    userId: UUID,
    cardioDiaryEntryId: UUID,
    update: CardioWorkout.Update
  ): Future[CardioWorkout]
  def getStrengthWorkoutsByIds(strengthDiaryEntryIds: Seq[UUID]): Future[Seq[StrengthWorkout]]
  def getStrengthWorkoutByIdForUser(userId: UUID, strengthDiaryEntryId: UUID): Future[Option[StrengthWorkout]]
  def updateStrengthWorkoutByIdForUser(
    userId: UUID,
    strengthDiaryEntryId: UUID,
    update: StrengthWorkout.Update
  ): Future[StrengthWorkout]
  def getAllCardioWorkoutsForDayByUser(
    userId: UUID,
    windowStart: Instant,
    windowEnd: Instant
  ): Future[Seq[CardioWorkout]]
  def insertCardioWorkoutForUser(id: UUID, userId: UUID, create: exercise.CardioWorkout.Create): Future[CardioWorkout]
  def deleteCardioWorkoutForUser(userId: UUID, id: UUID): Future[Unit]
  def getAllStrengthWorkoutsForDayByUser(
    userId: UUID,
    windowStart: Instant,
    windowEnd: Instant
  ): Future[Seq[StrengthWorkout]]
  def insertStrengthWorkoutForUser(id: UUID, userId: UUID, create: StrengthWorkout.Create): Future[StrengthWorkout]
  def deleteStrengthWorkoutForUser(userId: UUID, id: UUID): Future[Unit]
  def deleteAllCardioWorkoutsForUser(userId: UUID): Future[Unit]
  def deleteAllStrengthWorkoutsForUser(userId: UUID): Future[Unit]
  def getUserRecentlyViewedWorkoutIds(userId: UUID): Future[Seq[UUID]]
  def deleteMostRecentlyViewedWorkoutForUser(userId: UUID, workoutId: UUID): Future[Unit]
  def upsertMostRecentlyViewedWorkoutForUser(userId: UUID, workoutId: UUID): Future[Unit]
  def associateMeetupWithCardioEntryById(cardioEntryId: UUID, meetupId: UUID): Future[Unit]
  def dissociateMeetupFromCardioEntryById(cardioEntryId: UUID): Future[Unit]
  def associateMeetupWithStrengthEntryById(strengthEntryId: UUID, meetupId: UUID): Future[Unit]
  def dissociateMeetupFromStrengthEntryById(strengthEntryId: UUID): Future[Unit]
}
