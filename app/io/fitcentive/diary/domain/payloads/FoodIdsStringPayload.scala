package io.fitcentive.diary.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

case class FoodIdsStringPayload(foodIds: Seq[String])

object FoodIdsStringPayload {
  implicit lazy val reads: Reads[FoodIdsStringPayload] = Json.reads[FoodIdsStringPayload]
  implicit lazy val writes: Writes[FoodIdsStringPayload] = Json.writes[FoodIdsStringPayload]
}
