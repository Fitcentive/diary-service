package io.fitcentive.diary.controllers

import io.fitcentive.diary.api.DiaryApi
import io.fitcentive.diary.domain.exercise.{CardioWorkout, StrengthWorkout}
import io.fitcentive.diary.domain.food.FoodEntry
import io.fitcentive.diary.domain.payloads.{DiaryEntryIdsPayload, IntPayload, UUIDPayload}
import io.fitcentive.diary.infrastructure.utils.ServerErrorHandler
import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import java.time.{LocalDate, ZoneOffset}
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
  def updateCardioDiaryEntry(implicit userId: UUID, cardioEntryId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        validateJson[CardioWorkout.Update](request.body.asJson) { updatePayload =>
          diaryApi
            .updateCardioDiaryEntry(userId, cardioEntryId, updatePayload)
            .map(handleEitherResult(_)(cardioEntry => Ok(Json.toJson(cardioEntry))))
            .recover(resultErrorAsyncHandler)
        }
      }(request, userId)
    }

  def getCardioDiaryEntry(implicit userId: UUID, cardioEntryId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getCardioDiaryEntry(userId, cardioEntryId)
          .map(handleEitherResult(_)(cardioEntry => Ok(Json.toJson(cardioEntry))))
          .recover(resultErrorAsyncHandler)
      }(request, userId)
    }

  def updateStrengthDiaryEntry(implicit userId: UUID, strengthEntryId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        validateJson[StrengthWorkout.Update](request.body.asJson) { updatePayload =>
          diaryApi
            .updateStrengthDiaryEntry(userId, strengthEntryId, updatePayload)
            .map(handleEitherResult(_)(strengthEntry => Ok(Json.toJson(strengthEntry))))
            .recover(resultErrorAsyncHandler)
        }
      }(request, userId)
    }

  def getStrengthDiaryEntry(implicit userId: UUID, strengthEntryId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getStrengthDiaryEntry(userId, strengthEntryId)
          .map(handleEitherResult(_)(strengthEntry => Ok(Json.toJson(strengthEntry))))
          .recover(resultErrorAsyncHandler)
      }(request, userId)
    }

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

  def getAllDiaryEntriesForUserByDay(implicit
    userId: UUID,
    dateString: String,
    offsetInMinutes: Int,
  ): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getAllDiaryEntriesForUserByDay(
            userId,
            LocalDate.parse(dateString).atStartOfDay().toInstant(ZoneOffset.UTC),
            offsetInMinutes
          )
          .map(entries => Ok(Json.toJson(entries)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def associateDiaryEntriesToMeetup(implicit userId: UUID, meetupId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        validateJson[DiaryEntryIdsPayload](request.body.asJson) { payload =>
          diaryApi
            .associateDiaryEntriesToMeetup(meetupId, payload)
            .map(_ => NoContent)
            .recover(resultErrorAsyncHandler)
        }
      }(request, userId)
    }

  def getAllCardioWorkoutsForUserByDay(implicit
    userId: UUID,
    dateString: String,
    offsetInMinutes: Int
  ): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getCardioEntriesForUserByDay(
            userId,
            LocalDate.parse(dateString).atStartOfDay().toInstant(ZoneOffset.UTC),
            offsetInMinutes
          )
          .map(cardio => Ok(Json.toJson(cardio)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def getAllStrengthWorkoutsForUserByDay(implicit
    userId: UUID,
    dateString: String,
    offsetInMinutes: Int
  ): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getStrengthEntriesForUserByDay(
            userId,
            LocalDate.parse(dateString).atStartOfDay().toInstant(ZoneOffset.UTC),
            offsetInMinutes
          )
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
  def getAllFoodEntriesForUserByDay(implicit
    userId: UUID,
    dateString: String,
    offsetInMinutes: Int
  ): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getFoodEntriesForUserByDay(
            userId,
            LocalDate.parse(dateString).atStartOfDay().toInstant(ZoneOffset.UTC),
            offsetInMinutes
          )
          .map(foods => Ok(Json.toJson(foods)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def updateFoodDiaryEntry(implicit userId: UUID, foodEntryId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        validateJson[FoodEntry.Update](request.body.asJson) { updatePayload =>
          diaryApi
            .updateFoodDiaryEntry(userId, foodEntryId, updatePayload)
            .map(handleEitherResult(_)(foodEntry => Ok(Json.toJson(foodEntry))))
            .recover(resultErrorAsyncHandler)
        }
      }(request, userId)
    }

  def getFoodDiaryEntry(implicit userId: UUID, foodDiaryEntryId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        diaryApi
          .getFoodDiaryEntry(userId, foodDiaryEntryId)
          .map(handleEitherResult(_)(foodEntry => Ok(Json.toJson(foodEntry))))
          .recover(resultErrorAsyncHandler)
      }(request, userId)
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
          .map(foodIds => Ok(Json.toJson(foodIds)))
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

  //----------------------------------
  // Internal routes
  //----------------------------------
  def getDiaryEntriesForUserByIds: Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      validateJson[DiaryEntryIdsPayload](request.body.asJson) { payload =>
        diaryApi
          .getDiaryEntriesByIds(payload)
          .map(entries => Ok(Json.toJson(entries)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def dissociateFoodDiaryEntryFromMeetup(foodEntryId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      diaryApi
        .dissociateFoodDiaryEntryFromMeetup(foodEntryId)
        .map(_ => NoContent)
        .recover(resultErrorAsyncHandler)
    }

  def dissociateStrengthDiaryEntryFromMeetup(strengthEntryId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      diaryApi
        .dissociateStrengthDiaryEntryFromMeetup(strengthEntryId)
        .map(_ => NoContent)
        .recover(resultErrorAsyncHandler)
    }

  def dissociateCardioDiaryEntryFromMeetup(cardioEntryId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      diaryApi
        .dissociateCardioDiaryEntryFromMeetup(cardioEntryId)
        .map(_ => NoContent)
        .recover(resultErrorAsyncHandler)
    }
}
