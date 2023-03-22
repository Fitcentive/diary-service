package io.fitcentive.diary.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.diary.domain.food.FoodEntry
import io.fitcentive.diary.infrastructure.database.sql.AnormFoodDiaryRepository

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormFoodDiaryRepository])
trait FoodDiaryRepository {
  def getAllFoodEntriesForDayByUser(userId: UUID, day: Instant): Future[Seq[FoodEntry]]
  def insertFoodDiaryEntry(id: UUID, userId: UUID, create: FoodEntry.Create): Future[FoodEntry]
}
