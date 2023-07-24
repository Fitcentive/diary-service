package io.fitcentive.diary.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

case class StepsPayload(stepsTaken: Int, dateString: String)

object StepsPayload {
  implicit lazy val reads: Reads[StepsPayload] = Json.reads[StepsPayload]
  implicit lazy val writes: Writes[StepsPayload] = Json.writes[StepsPayload]
}
