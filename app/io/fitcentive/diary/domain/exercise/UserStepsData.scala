package io.fitcentive.diary.domain.exercise

import play.api.libs.json.{Json, OFormat}

import java.time.Instant
import java.util.UUID

case class UserStepsData(
  id: UUID,
  userId: UUID,
  steps: Int,
  caloriesBurned: Double,
  entryDate: String,
  createdAt: Instant,
  updatedAt: Instant
)

object UserStepsData {
  implicit lazy val format: OFormat[UserStepsData] = Json.format[UserStepsData]
}
