package io.fitcentive.diary.controllers

import io.fitcentive.diary.api.NutritionApi
import io.fitcentive.diary.domain.fatsecret.{FoodGetResult, FoodGetResultSingleServing}
import io.fitcentive.diary.domain.payloads.{FoodIdsStringPayload, IntPayload}
import io.fitcentive.diary.domain.user.FitnessUserProfile
import io.fitcentive.diary.infrastructure.utils.ServerErrorHandler
import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class NutritionController @Inject() (
  nutritionApi: NutritionApi,
  userAuthAction: UserAuthAction,
  internalAuthAction: InternalAuthAction,
  cc: ControllerComponents
)(implicit exec: ExecutionContext)
  extends AbstractController(cc)
  with PlayControllerOps
  with ServerErrorHandler {

  // -----------------------------
  // User Auth routes
  // -----------------------------
  def searchFoodsByExpression(query: String, pageNumber: Option[Int], maxResults: Option[Int]): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      nutritionApi
        .searchForFoods(query, pageNumber, maxResults)
        .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
        .recover(resultErrorAsyncHandler)
    }

  def getFoodsByIds: Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      validateJson[FoodIdsStringPayload](userRequest.request.body.asJson) { payload =>
        implicit lazy val writes: Writes[Either[FoodGetResult, FoodGetResultSingleServing]] = {
          case Left(value)  => Json.toJson(value)
          case Right(value) => Json.toJson(value)
        }

        nutritionApi
          .getFoodsByIds(payload.foodIds)
          .map(handleEitherResult(_)(foods => Ok(Json.toJson(foods))))
          .recover(resultErrorAsyncHandler)
      }
    }

  def getFoodById(foodId: String): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      nutritionApi
        .getFoodById(foodId)
        .map(handleEitherResult(_) {
          case Left(value)  => Ok(Json.toJson(value))
          case Right(value) => Ok(Json.toJson(value))
        })
        .recover(resultErrorAsyncHandler)
    }

  def autocompleteFood(query: String, maxResults: Option[Int]): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      nutritionApi
        .autocompleteFood(query, maxResults)
        .map(handleEitherResult(_)(suggestions => Ok(Json.toJson(suggestions))))
        .recover(resultErrorAsyncHandler)
    }

}
