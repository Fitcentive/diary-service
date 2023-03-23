package io.fitcentive.diary.infrastructure.database.sql

import anorm.{Macro, RowParser}
import io.fitcentive.diary.domain.food.{FoodEntry, MealEntry}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.sdk.utils.AnormOps
import io.fitcentive.diary.repositories.FoodDiaryRepository
import play.api.db.Database

import java.time.Instant
import java.util.{Date, UUID}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class AnormFoodDiaryRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends FoodDiaryRepository
  with DatabaseClient {

  import AnormFoodDiaryRepository._

  override def getAllFoodEntriesForDayByUser(userId: UUID, day: Instant): Future[Seq[FoodEntry]] =
    Future {
      getRecords(SQL_GET_FOOD_ENTRIES_FOR_USER_BY_DATE, "userId" -> userId, "entryDate" -> Date.from(day))(
        foodEntryRowParser
      ).map(_.toDomain)
    }

  override def insertFoodDiaryEntry(id: UUID, userId: UUID, create: FoodEntry.Create): Future[FoodEntry] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[FoodEntryRow](
          SQL_INSERT_AND_RETURN_FOOD_ENTRY,
          Seq(
            "id" -> id,
            "userId" -> userId,
            "foodId" -> create.foodId,
            "servingId" -> create.servingId,
            "numberOfServings" -> create.numberOfServings,
            "mealEntry" -> MealEntry(create.mealEntry).stringValue,
            "entryDate" -> create.entryDate,
            "now" -> now
          )
        )(foodEntryRowParser).toDomain
      }
    }

  override def deleteFoodDiaryEntry(userId: UUID, id: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_FOOD_DIARY_ENTRY, Seq("userId" -> userId, "foodEntryId" -> id))
    }
}

object AnormFoodDiaryRepository extends AnormOps {

  private val SQL_DELETE_FOOD_DIARY_ENTRY: String =
    """
      |delete from food_entries
      |where user_id = {userId}::uuid 
      |and id = {foodEntryId}::uuid ;
      |""".stripMargin

  private val SQL_INSERT_AND_RETURN_FOOD_ENTRY: String =
    """
      |insert into food_entries (id, user_id, food_id, serving_id, number_of_servings, meal_entry, entry_date, created_at, updated_at)
      |values ({id}::uuid, {userId}::uuid, {foodId}, {servingId}, {numberOfServings}, {mealEntry}, {entryDate}, {now}, {now})
      |returning *;
      |""".stripMargin

  private val SQL_GET_FOOD_ENTRIES_FOR_USER_BY_DATE: String =
    """
      |select * 
      |from food_entries
      |where user_id = {userId}::uuid
      |and entry_date::date = {entryDate}::date ;
      |""".stripMargin

  private case class FoodEntryRow(
    id: UUID,
    user_id: UUID,
    food_id: Int,
    serving_id: Int,
    number_of_servings: Double,
    meal_entry: String,
    entry_date: Instant,
    created_at: Instant,
    updated_at: Instant
  ) {
    def toDomain: FoodEntry =
      FoodEntry(
        id = id,
        userId = user_id,
        foodId = food_id,
        servingId = serving_id,
        numberOfServings = number_of_servings,
        mealEntry = MealEntry(meal_entry),
        entryDate = entry_date,
        createdAt = created_at,
        updatedAt = updated_at
      )
  }

  private val foodEntryRowParser: RowParser[FoodEntryRow] = Macro.namedParser[FoodEntryRow]
}
