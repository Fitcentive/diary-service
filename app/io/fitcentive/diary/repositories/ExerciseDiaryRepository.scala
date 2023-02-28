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
  def getAllCardioWorkoutsForDayByUser(userId: UUID, day: Instant): Future[Seq[CardioWorkout]]
  def insertCardioWorkoutForUser(id: UUID, userId: UUID, create: exercise.CardioWorkout.Create): Future[CardioWorkout]
  def getAllStrengthWorkoutsForDayByUser(userId: UUID, day: Instant): Future[Seq[StrengthWorkout]]
  def insertStrengthWorkoutForUser(id: UUID, userId: UUID, create: StrengthWorkout.Create): Future[StrengthWorkout]
}
