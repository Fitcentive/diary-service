package io.fitcentive.diary.domain.exercise

import play.api.libs.json.{Json, OFormat}

import java.time.Instant
import java.util.UUID

case class CardioWorkout(
  id: UUID,
  userId: UUID,
  workoutId: UUID,
  name: String,
  cardioDate: Instant,
  durationInMinutes: Option[Int],
  caloriesBurned: Option[Double],
  meetupId: Option[UUID],
  createdAt: Instant,
  updatedAt: Instant
)

object CardioWorkout {
  implicit lazy val format: OFormat[CardioWorkout] = Json.format[CardioWorkout]

  case class Create(
    workoutId: UUID,
    name: String,
    cardioDate: Instant,
    durationInMinutes: Option[Long],
    caloriesBurned: Option[Double],
    meetupId: Option[UUID],
  )
  object Create {
    implicit lazy val format: OFormat[CardioWorkout.Create] = Json.format[CardioWorkout.Create]
  }
}
