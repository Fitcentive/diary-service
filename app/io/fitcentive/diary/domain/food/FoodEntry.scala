package io.fitcentive.diary.domain.food

import play.api.libs.json.{Json, OFormat, Writes}

import java.time.Instant
import java.util.UUID

case class FoodEntry(
  id: UUID,
  userId: UUID,
  foodId: Int,
  servingId: Int,
  numberOfServings: Double,
  mealEntry: MealEntry,
  entryDate: Instant,
  createdAt: Instant,
  updatedAt: Instant,
)

object FoodEntry {
  implicit lazy val writes: Writes[FoodEntry] = Json.writes[FoodEntry]

  case class Create(foodId: Int, servingId: Int, numberOfServings: Double, mealEntry: String, entryDate: Instant)
  object Create {
    implicit lazy val format: OFormat[FoodEntry.Create] = Json.format[FoodEntry.Create]
  }

}
