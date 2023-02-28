package io.fitcentive.diary.domain.exercise

import play.api.libs.json.{Json, OFormat}

import java.time.Instant
import java.util.UUID

case class StrengthWorkout(
  id: UUID,
  userId: UUID,
  workoutId: UUID,
  name: String,
  exerciseDate: Instant,
  sets: Option[Long],
  reps: Option[Long],
  weightsInLbs: Seq[Long],
  caloriesBurned: Option[Double],
  meetupId: Option[UUID],
  createdAt: Instant,
  updatedAt: Instant
)

object StrengthWorkout {
  implicit lazy val format: OFormat[StrengthWorkout] = Json.format[StrengthWorkout]

  case class Create(
    workoutId: UUID,
    name: String,
    exerciseDate: Instant,
    sets: Option[Long],
    reps: Option[Long],
    weightsInLbs: Seq[Long],
    caloriesBurned: Option[Double],
    meetupId: Option[UUID],
  )
  object Create {
    implicit lazy val format: OFormat[StrengthWorkout.Create] = Json.format[StrengthWorkout.Create]
  }
}
