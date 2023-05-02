package io.fitcentive.diary.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.diary.domain.food.FoodEntry
import io.fitcentive.diary.infrastructure.database.sql.AnormFoodDiaryRepository

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormFoodDiaryRepository])
trait FoodDiaryRepository {
  def getAllFoodEntriesForDayByUser(userId: UUID, windowStart: Instant, windowEnd: Instant): Future[Seq[FoodEntry]]
  def insertFoodDiaryEntry(id: UUID, userId: UUID, create: FoodEntry.Create): Future[FoodEntry]
  def deleteFoodDiaryEntry(userId: UUID, id: UUID): Future[Unit]
  def deleteAllFoodEntriesForUser(userId: UUID): Future[Unit]
  def getUserRecentlyViewedFoodIds(userId: UUID): Future[Seq[Int]]
  def deleteMostRecentlyViewedFoodForUser(userId: UUID, foodId: Int): Future[Unit]
  def upsertMostRecentlyViewedFoodForUser(userId: UUID, foodId: Int): Future[Unit]
}
