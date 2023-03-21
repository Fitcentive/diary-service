package io.fitcentive.diary.api

import io.fitcentive.diary.domain.fatsecret.{FoodGetResult, FoodGetResultSingleServing, FoodSearchResults}
import io.fitcentive.diary.infrastructure.rest.RestFatsecretApiService
import io.fitcentive.diary.services.NutritionService
import io.fitcentive.sdk.error.DomainError

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NutritionApi @Inject() (nutritionService: NutritionService)(implicit ec: ExecutionContext) {

  def searchForFoods(
    query: String,
    pageNumber: Option[Int],
    maxResults: Option[Int]
  ): Future[Either[DomainError, FoodSearchResults]] =
    nutritionService.searchFoods(
      query,
      pageNumber.fold(0)(identity),
      maxResults.fold(RestFatsecretApiService.defaultMax)(identity)
    )

  def getFoodById(foodId: String): Future[Either[DomainError, Either[FoodGetResult, FoodGetResultSingleServing]]] =
    nutritionService.getFoodById(foodId)

}
