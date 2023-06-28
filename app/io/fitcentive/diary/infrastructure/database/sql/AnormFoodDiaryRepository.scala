package io.fitcentive.diary.infrastructure.database.sql

import anorm.{Column, Macro, MetaDataItem, RowParser, SqlParser, TypeDoesNotMatch}
import io.circe.{parser, Json}
import io.fitcentive.diary.domain.fatsecret.{FoodGetResult, FoodGetResultSingleServing}
import io.fitcentive.diary.domain.food.{FoodEntry, MealEntry}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.sdk.utils.AnormOps
import io.fitcentive.diary.repositories.FoodDiaryRepository
import play.api.db.Database

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps
import io.circe.syntax._
import io.circe.generic.auto._
import io.fitcentive.diary.infrastructure.database.sql.AnormExerciseDiaryRepository.{
  transformUuidsToSql,
  SQL_ASSOCIATE_MEETUP_FROM_STRENGTH_ENTRY
}

@Singleton
class AnormFoodDiaryRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends FoodDiaryRepository
  with DatabaseClient {

  import AnormFoodDiaryRepository._

  override def getUserRecentlyViewedFoodIds(userId: UUID): Future[Seq[Int]] =
    Future {
      getRecords(SQL_GET_USER_RECENTLY_VIEWED_FOODS, "userId" -> userId)(SqlParser.scalar[Int])
    }

  override def associateMeetupWithFoodEntryById(foodEntryId: UUID, meetupId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(
        SQL_ASSOCIATE_MEETUP_FROM_FOOD_ENTRY,
        Seq("foodEntryId" -> foodEntryId, "meetupId" -> meetupId)
      )
    }

  override def dissociateMeetupFromFoodEntryById(foodEntryId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DISSOCIATE_MEETUP_FROM_FOOD_ENTRY, Seq("foodEntryId" -> foodEntryId))
    }

  override def deleteMostRecentlyViewedFoodForUser(userId: UUID, foodId: Int): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_USER_RECENTLY_VIEWED_FOODS, Seq("userId" -> userId, "foodId" -> foodId))
    }

  override def upsertMostRecentlyViewedFoodForUser(userId: UUID, foodId: Int): Future[Unit] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithoutReturning(
          SQL_UPSERT_USER_RECENTLY_VIEWED_FOODS,
          Seq("id" -> UUID.randomUUID(), "userId" -> userId, "foodId" -> foodId, "now" -> now)
        )
      }
    }

  override def deleteAllFoodEntriesForUser(userId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_ALL_FOOD_DIARY_ENTRIES, Seq("userId" -> userId))
    }

  override def getFoodEntryForUserById(userId: UUID, foodDiaryEntryId: UUID): Future[Option[FoodEntry]] =
    Future {
      getRecordOpt(SQL_GET_FOOD_ENTRY_FOR_USER_BY_ID, "userId" -> userId, "id" -> foodDiaryEntryId)(foodEntryRowParser)
        .map(_.toDomain)
    }

  override def updateFoodEntryForUserById(
    userId: UUID,
    foodDiaryEntryId: UUID,
    update: FoodEntry.Update
  ): Future[FoodEntry] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn(
          SQL_UPDATE_FOOD_ENTRY_FOR_USER_BY_ID,
          Seq(
            "userId" -> userId,
            "id" -> foodDiaryEntryId,
            "servingId" -> update.servingId,
            "numberOfServings" -> update.numberOfServings,
            "entryDate" -> update.entryDate,
            "now" -> now,
            "meetupId" -> update.meetupId,
          )
        )(foodEntryRowParser).toDomain
      }
    }

  override def getAllFoodEntriesForDayByUser(
    userId: UUID,
    windowStart: Instant,
    windowEnd: Instant
  ): Future[Seq[FoodEntry]] =
    Future {
      getRecords(
        SQL_GET_FOOD_ENTRIES_FOR_USER_BY_DATE,
        "userId" -> userId,
        "windowStart" -> windowStart,
        "windowEnd" -> windowEnd
      )(foodEntryRowParser).map(_.toDomain)
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
            "now" -> now,
            "meetupId" -> create.meetupId,
          )
        )(foodEntryRowParser).toDomain
      }
    }

  override def deleteFoodDiaryEntry(userId: UUID, id: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_FOOD_DIARY_ENTRY, Seq("userId" -> userId, "foodEntryId" -> id))
    }

  override def getCachedDataForFoodId(
    foodId: String
  ): Future[Option[Either[FoodGetResult, FoodGetResultSingleServing]]] =
    Future {
      getRecordOpt[FatsecretFoodCacheRow](SQL_GET_FATSECRET_CACHED_FOOD_BY_FOOD_ID, "foodId" -> foodId)(
        fatsecretFoodCacheRowParser
      ).map(_.toDomain)
    }

  override def upsertCachedDataForFoodId(
    foodId: String,
    foodData: Json
  ): Future[Either[FoodGetResult, FoodGetResultSingleServing]] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[FatsecretFoodCacheRow](
          SQL_UPSERT_FATSECRET_CACHED_FOOD_BY_FOOD_ID,
          Seq("foodId" -> foodId, "foodData" -> foodData.asJson.toString(), "now" -> now)
        )(fatsecretFoodCacheRowParser).toDomain
      }
    }

  override def getFoodEntriesByIds(foodDiaryEntryIds: Seq[UUID]): Future[Seq[FoodEntry]] =
    Future {
      getRecords(SQL_GET_FOOD_ENTRIES_BY_IDS(foodDiaryEntryIds))(foodEntryRowParser)
        .map(_.toDomain)
    }
}

object AnormFoodDiaryRepository extends AnormOps {

  private val SQL_DELETE_ALL_FOOD_DIARY_ENTRIES: String =
    """
      |delete from food_entries
      |where user_id = {userId}::uuid ;
      |""".stripMargin

  private val SQL_DELETE_FOOD_DIARY_ENTRY: String =
    """
      |delete from food_entries
      |where user_id = {userId}::uuid 
      |and id = {foodEntryId}::uuid ;
      |""".stripMargin

  private val SQL_INSERT_AND_RETURN_FOOD_ENTRY: String =
    """
      |insert into food_entries (id, user_id, food_id, serving_id, number_of_servings, meal_entry, entry_date, created_at, updated_at, meetup_id)
      |values ({id}::uuid, {userId}::uuid, {foodId}, {servingId}, {numberOfServings}, {mealEntry}, {entryDate}, {now}, {now}, {meetupId}::uuid)
      |returning *;
      |""".stripMargin

  private val SQL_UPDATE_FOOD_ENTRY_FOR_USER_BY_ID: String =
    """
      |update food_entries
      |set
      | serving_id = {servingId},
      | number_of_servings = {numberOfServings},
      | entry_date = {entryDate},
      | meetup_id = {meetupId}::uuid,
      | updated_at = {now}
      |where user_id = {userId}::uuid
      |and id = {id}::uuid 
      |returning *;
      |""".stripMargin

  private val SQL_GET_FOOD_ENTRY_FOR_USER_BY_ID: String =
    """
      |select *
      |from food_entries
      |where user_id = {userId}::uuid
      |and id = {id}::uuid ;
      |""".stripMargin

  private def SQL_GET_FOOD_ENTRIES_BY_IDS(ids: Seq[UUID]): String = {
    val sql = """
                |select *
                |from food_entries
                |where id in (
                |""".stripMargin
    transformUuidsToSql(ids, sql)
  }

  private val SQL_GET_FOOD_ENTRIES_FOR_USER_BY_DATE: String =
    """
      |select * 
      |from food_entries
      |where user_id = {userId}::uuid
      |and entry_date >= {windowStart} 
      |and entry_date <= {windowEnd} ;
      |""".stripMargin

  private val SQL_GET_USER_RECENTLY_VIEWED_FOODS: String =
    """
      |select food_id
      |from user_recently_viewed_foods
      |where user_id = {userId}::uuid
      |order by last_accessed desc ;
      |""".stripMargin

  private val SQL_ASSOCIATE_MEETUP_FROM_FOOD_ENTRY: String =
    """
      |update food_entries
      |set meetup_id = {meetupId}::uuid
      |where id = {foodEntryId}::uuid ;
      |""".stripMargin

  private val SQL_DISSOCIATE_MEETUP_FROM_FOOD_ENTRY: String =
    """
      |update food_entries
      |set meetup_id = null
      |where id = {foodEntryId}::uuid ;
      |""".stripMargin

  private val SQL_DELETE_USER_RECENTLY_VIEWED_FOODS: String =
    """
      |delete from user_recently_viewed_foods
      |where user_id = {userId}::uuid 
      |and food_id = {foodId} ;
      |""".stripMargin

  private val SQL_UPSERT_USER_RECENTLY_VIEWED_FOODS: String =
    """
      |insert into user_recently_viewed_foods (id, user_id, food_id, last_accessed, created_at, updated_at)
      |values ({id}::uuid, {userId}:: uuid, {foodId}, {now}, {now}, {now}) 
      |on conflict (user_id, food_id) 
      |do update set 
      |last_accessed = {now};
      |""".stripMargin

  private val SQL_UPSERT_FATSECRET_CACHED_FOOD_BY_FOOD_ID: String =
    """
      |insert into fatsecret_food_cache (food_id, food_data, created_at, updated_at)
      |values ({foodId}, {foodData}::jsonb, {now}, {now})
      |on conflict (food_id)
      |do
      | update set 
      |   food_data = {foodData}::jsonb,
      |   updated_at = {now}
      |returning * ;
      |""".stripMargin

  private val SQL_GET_FATSECRET_CACHED_FOOD_BY_FOOD_ID: String =
    """
      |select *
      |from fatsecret_food_cache
      |where food_id = {foodId} ;
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
    updated_at: Instant,
    meetup_id: Option[UUID],
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
        updatedAt = updated_at,
        meetupId = meetup_id,
      )
  }

  private case class FatsecretFoodCacheRow(food_id: String, food_data: Json, created_at: Instant, updated_at: Instant) {
    def toDomain: Either[FoodGetResult, FoodGetResultSingleServing] = {
      decodeFoodData(food_data).getOrElse(throw new IllegalArgumentException("Unable to decode food data!"))
    }
  }

  implicit lazy val columnToJson: Column[Json] =
    Column.nonNull { (value, meta) =>
      val MetaDataItem(qualified, _, _) = meta
      value match {
        case json: org.postgresql.util.PGobject =>
          Right(parser.parse(json.getValue).getOrElse(throw new Exception("Circe parsing exception")))
        case _ =>
          Left(
            TypeDoesNotMatch(
              s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to Json for column $qualified"
            )
          )
      }
    }

  private def decodeFoodData(foodData: Json): Option[Either[FoodGetResult, FoodGetResultSingleServing]] = {
    foodData
      .as[FoodGetResult]
      .pipe {
        case Right(value) =>
          Some(Left(value))

        case Left(_) =>
          foodData
            .as[FoodGetResultSingleServing]
            .pipe {
              case Right(value) => Some(Right(value))
              case Left(_)      => Option.empty
            }
      }
  }

  private val foodEntryRowParser: RowParser[FoodEntryRow] = Macro.namedParser[FoodEntryRow]
  private val fatsecretFoodCacheRowParser: RowParser[FatsecretFoodCacheRow] = Macro.namedParser[FatsecretFoodCacheRow]
}
