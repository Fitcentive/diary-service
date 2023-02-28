package io.fitcentive.diary.api

import io.fitcentive.diary.domain.exercise.{CardioWorkout, StrengthWorkout}
import io.fitcentive.diary.repositories.ExerciseDiaryRepository

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DiaryApi @Inject() (exerciseDiaryRepository: ExerciseDiaryRepository)(implicit ec: ExecutionContext) {

  def insertCardioDiaryEntry(userId: UUID, create: CardioWorkout.Create): Future[CardioWorkout] =
    exerciseDiaryRepository
      .insertCardioWorkoutForUser(id = UUID.randomUUID(), userId = userId, create = create)

  def insertStrengthDiaryEntry(userId: UUID, create: StrengthWorkout.Create): Future[StrengthWorkout] =
    exerciseDiaryRepository
      .insertStrengthWorkoutForUser(id = UUID.randomUUID(), userId = userId, create = create)

  def getCardioEntriesForUserByDay(userId: UUID, day: Instant): Future[Seq[CardioWorkout]] =
    exerciseDiaryRepository
      .getAllCardioWorkoutsForDayByUser(userId = userId, day = day)

  def getStrengthEntriesForUserByDay(userId: UUID, day: Instant): Future[Seq[StrengthWorkout]] =
    exerciseDiaryRepository
      .getAllStrengthWorkoutsForDayByUser(userId = userId, day = day)

}
