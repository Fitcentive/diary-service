package io.fitcentive.diary.controllers

import io.fitcentive.diary.api.NutritionApi
import io.fitcentive.diary.infrastructure.utils.ServerErrorHandler
import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import play.api.libs.json.Json
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

}
