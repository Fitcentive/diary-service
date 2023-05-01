package io.fitcentive.diary.controllers

import io.fitcentive.diary.api.{DiaryApi, ExerciseApi}
import io.fitcentive.diary.domain.exercise.{CardioWorkout, StrengthWorkout}
import io.fitcentive.diary.domain.food.FoodEntry
import io.fitcentive.diary.domain.payloads.{IntPayload, UUIDPayload}
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

  // --------------------------------
  // Exercise Diary API methods
  // --------------------------------
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

  def deleteCardioEntryFromDiary(implicit userId: UUID, cardioEntryId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .deleteCardioDiaryEntry(userId, cardioEntryId)
          .map(cardio => Ok)
          .recover(resultErrorAsyncHandler)
      }(request, userId)
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

  def deleteStrengthEntryFromDiary(implicit userId: UUID, strengthEntryId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .deleteStrengthDiaryEntry(userId, strengthEntryId)
          .map(cardio => Ok)
          .recover(resultErrorAsyncHandler)
      }(request, userId)
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

  def getUserMostRecentlyViewedWorkouts(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getUserMostRecentlyViewedWorkoutIds(userId)
          .map(userIds => Ok(Json.toJson(userIds)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def addUserMostRecentlyViewedWorkout(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        validateJson[UUIDPayload](request.body.asJson) { payload =>
          diaryApi
            .addUserMostRecentlyViewedWorkout(userId, payload.id)
            .map(_ => NoContent)
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  // --------------------------------
  // Food Diary API methods
  // --------------------------------
  def getAllFoodEntriesForUserByDay(implicit userId: UUID, dateString: String): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getFoodEntriesForUserByDay(userId, LocalDate.parse(dateString).atStartOfDay().toInstant(ZoneOffset.UTC))
          .map(foods => Ok(Json.toJson(foods)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def addFoodEntryToDiary(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        validateJson[FoodEntry.Create](request.body.asJson) { createPayload =>
          diaryApi
            .insertFoodDiaryEntry(userId, createPayload)
            .map(cardio => Ok(Json.toJson(cardio)))
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  def deleteFoodEntryFromDiary(implicit userId: UUID, foodEntryId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .deleteFoodDiaryEntry(userId, foodEntryId)
          .map(_ => Ok)
          .recover(resultErrorAsyncHandler)
      }(request, userId)
    }

  def getUserMostRecentlyViewedFoods(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getUserMostRecentlyViewedFoodIds(userId)
          .map(userIds => Ok(Json.toJson(userIds)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def addUserMostRecentlyViewedFood(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        validateJson[IntPayload](request.body.asJson) { payload =>
          diaryApi
            .addUserMostRecentlyViewedFood(userId, payload.id)
            .map(_ => NoContent)
            .recover(resultErrorAsyncHandler)
        }
      }
    }
}
