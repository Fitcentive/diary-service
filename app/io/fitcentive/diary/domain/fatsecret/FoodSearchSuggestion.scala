package io.fitcentive.diary.domain.fatsecret

import play.api.libs.json.{Json, OFormat}

case class FoodSearchSuggestions(suggestions: Option[FoodSearchSuggestion])
object FoodSearchSuggestions {
  implicit lazy val format: OFormat[FoodSearchSuggestions] = Json.format[FoodSearchSuggestions]
}

case class FoodSearchSuggestion(suggestion: Seq[String])
object FoodSearchSuggestion {
  implicit lazy val format: OFormat[FoodSearchSuggestion] = Json.format[FoodSearchSuggestion]
}
