package io.fitcentive.diary.controllers

import io.fitcentive.diary.api.{DiaryApi, ExerciseApi}
import io.fitcentive.diary.domain.exercise.{CardioWorkout, StrengthWorkout}
import io.fitcentive.diary.infrastructure.utils.ServerErrorHandler
import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import java.time.{Instant, LocalDate, ZoneOffset}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DiaryController @Inject() (
  diaryApi: DiaryApi,
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
  def addCardioEntryToDiary(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        validateJson[CardioWorkout.Create](request.body.asJson) { createPayload =>
          diaryApi
            .insertCardioDiaryEntry(userId, createPayload)
            .map(cardio => Ok(Json.toJson(cardio)))
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  def addStrengthEntryToDiary(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        validateJson[StrengthWorkout.Create](request.body.asJson) { createPayload =>
          diaryApi
            .insertStrengthDiaryEntry(userId, createPayload)
            .map(cardio => Ok(Json.toJson(cardio)))
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  def getAllCardioWorkoutsForUserByDay(implicit userId: UUID, dateString: String): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getCardioEntriesForUserByDay(userId, LocalDate.parse(dateString).atStartOfDay().toInstant(ZoneOffset.UTC))
          .map(cardio => Ok(Json.toJson(cardio)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def getAllStrengthWorkoutsForUserByDay(implicit userId: UUID, dateString: String): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getStrengthEntriesForUserByDay(userId, LocalDate.parse(dateString).atStartOfDay().toInstant(ZoneOffset.UTC))
          .map(cardio => Ok(Json.toJson(cardio)))
          .recover(resultErrorAsyncHandler)
      }
    }

}
