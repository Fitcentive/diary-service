package io.fitcentive.diary.api

import cats.data.EitherT
import io.circe.Json
import io.fitcentive.diary.domain.errors.FatsecretApiError
import io.fitcentive.diary.domain.fatsecret.{
  FoodGetResult,
  FoodGetResultSingleServing,
  FoodSearchResults,
  FoodSearchSuggestions
}
import io.fitcentive.diary.infrastructure.rest.RestFatsecretApiService
import io.fitcentive.diary.repositories.FoodDiaryRepository
import io.fitcentive.diary.services.NutritionService
import io.fitcentive.sdk.error.DomainError
import io.circe.syntax._
import io.circe.generic.auto._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NutritionApi @Inject() (nutritionService: NutritionService, foodDiaryRepository: FoodDiaryRepository)(implicit
  ec: ExecutionContext
) {

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

  def getFoodById(foodId: String): Future[Either[DomainError, Either[FoodGetResult, FoodGetResultSingleServing]]] = {
    foodDiaryRepository
      .getCachedDataForFoodId(foodId)
      .flatMap {
        case Some(value) => Future.successful(Right(value))
        case None => {
            (for {
              data <- EitherT[Future, DomainError, Either[FoodGetResult, FoodGetResultSingleServing]](
                nutritionService.getFoodById(foodId)
              )
              foodId = {
                if (data.isLeft) data.swap.map(_.food.foodId).getOrElse("bad_food_state_id")
                else data.map(_.food.foodId).getOrElse("bad_food_state_id")
              }
              foodData = {
                if (data.isLeft) data.swap.map(_.asJson).getOrElse(Json.obj())
                else data.map(_.asJson).getOrElse(Json.obj())
              }
              _ <- EitherT.right[DomainError](foodDiaryRepository.upsertCachedDataForFoodId(foodId, foodData))
            } yield data).value
          }.map(
            _.map(Right.apply)
              .getOrElse(Left(FatsecretApiError("An error occurred while trying to fetch and fill cache")))
          )
      }
  }

  def autocompleteFood(query: String, maxResults: Option[Int]): Future[Either[DomainError, FoodSearchSuggestions]] =
    nutritionService.autoCompleteFoods(
      query,
      maxResults.fold(RestFatsecretApiService.defaultAutocompleteMaxResults)(identity)
    )

}
