package io.fitcentive.diary.domain.fatsecret

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Json, JsonConfiguration, OFormat}

case class FoodGetResult(food: FoodResult)
object FoodGetResult {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[FoodGetResult] = Json.format[FoodGetResult]
}

case class FoodResult(foodId: String, foodName: String, foodType: String, foodUrl: String, servings: Servings)
object FoodResult {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[FoodResult] = Json.format[FoodResult]
}

case class Servings(serving: Seq[Serving])
object Servings {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[Servings] = Json.format[Servings]
}

case class Serving(
  calcium: Option[String],
  calories: Option[String],
  carbohydrate: Option[String],
  cholesterol: Option[String],
  fat: Option[String],
  fiber: Option[String],
  iron: Option[String],
  measurementDescription: Option[String],
  metricServingAmount: Option[String],
  metricServingUnit: Option[String],
  monounsaturatedFat: Option[String],
  numberOfUnits: Option[String],
  polyunsaturatedFat: Option[String],
  potassium: Option[String],
  protein: Option[String],
  saturatedFat: Option[String],
  servingDescription: Option[String],
  servingId: Option[String],
  servingUrl: Option[String],
  sodium: Option[String],
  sugar: Option[String],
  // todo - cant have more than 22 fields with play json parser
//  vitaminA: Option[String],
//  vitaminC: Option[String],
)
object Serving {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[Serving] = Json.format[Serving]
}
