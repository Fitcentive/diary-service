package io.fitcentive.diary.domain.fatsecret

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Json, JsonConfiguration, OFormat}

case class FoodSearchResults(foods: FoodResults)
object FoodSearchResults {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[FoodSearchResults] = Json.format[FoodSearchResults]
}

case class FoodResults(food: Seq[FoodSearchResult], maxResults: String, pageNumber: String, totalResults: String)
object FoodResults {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[FoodResults] = Json.format[FoodResults]
}

case class FoodSearchResult(
  brandName: Option[String],
  foodDescription: String,
  foodId: String,
  foodName: String,
  foodType: String,
  foodUrl: String
)
object FoodSearchResult {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[FoodSearchResult] = Json.format[FoodSearchResult]
}
