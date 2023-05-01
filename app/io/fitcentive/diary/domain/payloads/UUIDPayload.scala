package io.fitcentive.diary.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

import java.util.UUID

case class UUIDPayload(id: UUID)

object UUIDPayload {
  implicit lazy val reads: Reads[UUIDPayload] = Json.reads[UUIDPayload]
  implicit lazy val writes: Writes[UUIDPayload] = Json.writes[UUIDPayload]
}
