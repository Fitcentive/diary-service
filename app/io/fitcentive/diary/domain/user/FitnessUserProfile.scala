package io.fitcentive.diary.domain.user

import play.api.libs.json.{Json, Reads, Writes}

import java.time.Instant
import java.util.UUID

case class FitnessUserProfile(
  userId: UUID,
  heightInCm: Double,
  weightInLbs: Double,
  createdAt: Instant,
  updatedAt: Instant
)

object FitnessUserProfile {
  implicit lazy val writes: Writes[FitnessUserProfile] = Json.writes[FitnessUserProfile]

  case class Update(heightInCm: Double, weightInLbs: Double)

  object Update {
    implicit lazy val reads: Reads[FitnessUserProfile.Update] = Json.reads[FitnessUserProfile.Update]
    implicit lazy val writes: Writes[FitnessUserProfile.Update] = Json.writes[FitnessUserProfile.Update]
  }
}
