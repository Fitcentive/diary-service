package io.fitcentive.diary.controllers

import io.fitcentive.diary.api.UserApi
import io.fitcentive.diary.domain.user.FitnessUserProfile
import io.fitcentive.diary.infrastructure.utils.ServerErrorHandler
import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject() (
  userApi: UserApi,
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
  def getUserFitnessProfile(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userApi
          .getUserFitnessProfile(userId)
          .map(handleEitherResult(_)(profile => Ok(Json.toJson(profile))))
          .recover(resultErrorAsyncHandler)
      }
    }

  def upsertUserFitnessProfile(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        validateJson[FitnessUserProfile.Update](userRequest.request.body.asJson) { userProfileUpdate =>
          userApi
            .upsertUserFitnessProfile(userId, userProfileUpdate)
            .map(handleEitherResult(_)(userProfile => Ok(Json.toJson(userProfile))))
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  // -----------------------------
  // Internal Auth routes
  // -----------------------------
  def deleteUserDiaryData(implicit userId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit userRequest =>
      userApi
        .deleteUserDiaryData(userId)
        .map(_ => NoContent)
        .recover(resultErrorAsyncHandler)
    }

}
