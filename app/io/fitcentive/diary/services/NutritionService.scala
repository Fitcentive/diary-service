package io.fitcentive.diary.services

import com.google.inject.ImplementedBy
import io.fitcentive.diary.domain.fatsecret.{
  FoodGetResult,
  FoodGetResultSingleServing,
  FoodSearchResults,
  FoodSearchSuggestions
}
import io.fitcentive.diary.infrastructure.rest.RestFatsecretApiService
import io.fitcentive.sdk.error.DomainError

import scala.concurrent.Future

@ImplementedBy(classOf[RestFatsecretApiService])
trait NutritionService {
  def autoCompleteFoods(
    query: String,
    maxResults: Int = RestFatsecretApiService.defaultAutocompleteMaxResults
  ): Future[Either[DomainError, FoodSearchSuggestions]]
  def searchFoods(
    query: String,
    pageNumber: Int = 0,
    maxResults: Int = RestFatsecretApiService.defaultMax
  ): Future[Either[DomainError, FoodSearchResults]]
  def getFoodById(foodId: String): Future[Either[DomainError, Either[FoodGetResult, FoodGetResultSingleServing]]]
  def getAuthToken: Future[Either[DomainError, String]]
}
