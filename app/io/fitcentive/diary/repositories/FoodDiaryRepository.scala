package io.fitcentive.diary.repositories

import com.google.inject.ImplementedBy
import io.circe.Json
import io.fitcentive.diary.domain.fatsecret.{FoodGetResult, FoodGetResultSingleServing}
import io.fitcentive.diary.domain.food.FoodEntry
import io.fitcentive.diary.infrastructure.database.sql.AnormFoodDiaryRepository

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormFoodDiaryRepository])
trait FoodDiaryRepository {
  def associateMeetupWithFoodEntryById(foodEntryId: UUID, meetupId: UUID): Future[Unit]
  def dissociateMeetupFromFoodEntryById(foodEntryId: UUID): Future[Unit]
  def getAllFoodEntriesForDayByUser(userId: UUID, windowStart: Instant, windowEnd: Instant): Future[Seq[FoodEntry]]
  def getCountOfFoodEntriesForDayByUser(userId: UUID, windowStart: Instant, windowEnd: Instant): Future[Int]
  def getFoodEntryForUserById(userId: UUID, foodDiaryEntryId: UUID): Future[Option[FoodEntry]]
  def getFoodEntriesByIds(foodDiaryEntryIds: Seq[UUID]): Future[Seq[FoodEntry]]
  def updateFoodEntryForUserById(userId: UUID, foodDiaryEntryId: UUID, update: FoodEntry.Update): Future[FoodEntry]
  def insertFoodDiaryEntry(id: UUID, userId: UUID, create: FoodEntry.Create): Future[FoodEntry]
  def deleteFoodDiaryEntry(userId: UUID, id: UUID): Future[Unit]
  def deleteAllFoodEntriesForUser(userId: UUID): Future[Unit]
  def getUserRecentlyViewedFoodIds(userId: UUID): Future[Seq[Int]]
  def deleteMostRecentlyViewedFoodForUser(userId: UUID, foodId: Int): Future[Unit]
  def upsertMostRecentlyViewedFoodForUser(userId: UUID, foodId: Int): Future[Unit]
  def getCachedDataForFoodId(foodId: String): Future[Option[Either[FoodGetResult, FoodGetResultSingleServing]]]
  def upsertCachedDataForFoodId(
    foodId: String,
    foodData: Json
  ): Future[Either[FoodGetResult, FoodGetResultSingleServing]]
}
