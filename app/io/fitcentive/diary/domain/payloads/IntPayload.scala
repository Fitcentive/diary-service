package io.fitcentive.diary.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

case class IntPayload(id: Int)

object IntPayload {
  implicit lazy val reads: Reads[IntPayload] = Json.reads[IntPayload]
  implicit lazy val writes: Writes[IntPayload] = Json.writes[IntPayload]
}
