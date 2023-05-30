package io.fitcentive.diary.domain.diary

import io.fitcentive.diary.domain.exercise.{CardioWorkout, StrengthWorkout}
import io.fitcentive.diary.domain.food.FoodEntry
import play.api.libs.json.{Json, Writes}

case class AllDiaryEntriesForDay(
  cardioWorkouts: Seq[CardioWorkout],
  strengthWorkouts: Seq[StrengthWorkout],
  foodEntries: Seq[FoodEntry]
)

object AllDiaryEntriesForDay {
  implicit lazy val writes: Writes[AllDiaryEntriesForDay] = Json.writes[AllDiaryEntriesForDay]
}
