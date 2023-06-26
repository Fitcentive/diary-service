package io.fitcentive.diary.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

import java.util.UUID

case class DiaryEntryIdsPayload(foodEntryIds: Seq[UUID], cardioEntryIds: Seq[UUID], strengthEntryIds: Seq[UUID])

object DiaryEntryIdsPayload {
  implicit lazy val reads: Reads[DiaryEntryIdsPayload] = Json.reads[DiaryEntryIdsPayload]
  implicit lazy val writes: Writes[DiaryEntryIdsPayload] = Json.writes[DiaryEntryIdsPayload]
}
