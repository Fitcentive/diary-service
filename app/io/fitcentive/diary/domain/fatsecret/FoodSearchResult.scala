package io.fitcentive.diary.domain.fatsecret

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Json, JsonConfiguration, OFormat}

case class FoodSearchResults(foods: FoodResults) {
  def toOut: FoodSearchResultsOut = FoodSearchResultsOut(foods = foods.toOut)
}
case class FoodSearchResultsOut(foods: FoodResultsOut)
object FoodSearchResultsOut {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[FoodSearchResultsOut] = Json.format[FoodSearchResultsOut]
}
object FoodSearchResults {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[FoodSearchResults] = Json.format[FoodSearchResults]
}

case class FoodResults(
  food: Option[Seq[FoodSearchResult]],
  maxResults: String,
  pageNumber: String,
  totalResults: String
) {
  def toOut: FoodResultsOut =
    FoodResultsOut(
      food = food.getOrElse(Seq.empty),
      maxResults = maxResults,
      pageNumber = pageNumber,
      totalResults = totalResults
    )
}
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

case class FoodResultsOut(food: Seq[FoodSearchResult], maxResults: String, pageNumber: String, totalResults: String)
object FoodResultsOut {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[FoodResultsOut] = Json.format[FoodResultsOut]
}
