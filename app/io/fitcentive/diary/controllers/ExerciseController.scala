package io.fitcentive.diary.controllers

import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import io.fitcentive.diary.api.ExerciseApi
import io.fitcentive.diary.infrastructure.utils.ServerErrorHandler
import play.api.libs.json.Json
import play.api.mvc._

import java.util.UUID
import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class ExerciseController @Inject() (
  exerciseApi: ExerciseApi,
  userAuthAction: UserAuthAction,
  internalAuthAction: InternalAuthAction,
  cc: ControllerComponents
)(implicit exec: ExecutionContext)
  extends AbstractController(cc)
  with PlayControllerOps
  with ServerErrorHandler {

  // -----------------------------
  // Unauthenticated routes
  // -----------------------------
  def getAllExerciseInfo: Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      exerciseApi.getAllExerciseInfo
        .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
        .recover(resultErrorAsyncHandler)
    }

}
