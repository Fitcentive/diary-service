package io.fitcentive.diary.domain.diary

import play.api.libs.json.{Json, Writes}

case class AllDiaryEntriesForMonth(entries: Map[String, AllDiaryEntriesForDay])

object AllDiaryEntriesForMonth {
  implicit lazy val writes: Writes[AllDiaryEntriesForMonth] = Json.writes[AllDiaryEntriesForMonth]
}
