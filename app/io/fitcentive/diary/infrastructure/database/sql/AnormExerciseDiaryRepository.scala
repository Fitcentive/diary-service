package io.fitcentive.diary.infrastructure.database.sql

import anorm.{Macro, RowParser, SqlParser}
import io.fitcentive.diary.domain.exercise.{CardioWorkout, StrengthWorkout}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.sdk.utils.AnormOps
import io.fitcentive.diary.repositories.ExerciseDiaryRepository
import play.api.db.Database

import java.time.Instant
import java.util.{Date, UUID}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class AnormExerciseDiaryRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends ExerciseDiaryRepository
  with DatabaseClient {

  import AnormExerciseDiaryRepository._

  override def getUserRecentlyViewedWorkoutIds(userId: UUID): Future[Seq[UUID]] =
    Future {
      getRecords(SQL_GET_USER_RECENTLY_VIEWED_WORKOUTS, "userId" -> userId)(SqlParser.scalar[UUID])
    }

  override def deleteMostRecentlyViewedWorkoutForUser(userId: UUID, workoutId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(
        SQL_DELETE_USER_RECENTLY_VIEWED_WORKOUTS,
        Seq("userId" -> userId, "workoutId" -> workoutId)
      )
    }

  override def addMostRecentlyViewedWorkoutForUser(userId: UUID, workoutId: UUID): Future[Unit] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithoutReturning(
          SQL_ADD_USER_RECENTLY_VIEWED_WORKOUTS,
          Seq("userId" -> userId, "workoutId" -> workoutId, "now" -> now)
        )
      }
    }

  override def deleteAllCardioWorkoutsForUser(userId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_ALL_CARDIO_WORKOUT_ENTRIES, Seq("userId" -> userId))
    }

  override def deleteAllStrengthWorkoutsForUser(userId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_ALL_STRENGTH_WORKOUT_ENTRIES, Seq("userId" -> userId))
    }

  override def getAllCardioWorkoutsForDayByUser(userId: UUID, day: Instant): Future[Seq[CardioWorkout]] =
    Future {
      getRecords(SQL_GET_CARDIO_WORKOUTS_FOR_USER_BY_DATE, "userId" -> userId, "cardioDate" -> Date.from(day))(
        cardioWorkoutRowParser
      ).map(_.toDomain)
    }

  override def insertCardioWorkoutForUser(id: UUID, userId: UUID, create: CardioWorkout.Create): Future[CardioWorkout] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[CardioWorkoutRow](
          SQL_INSERT_AND_RETURN_CARDIO_WORKOUT,
          Seq(
            "id" -> id,
            "userId" -> userId,
            "workoutId" -> create.workoutId,
            "name" -> create.name,
            "cardioDate" -> create.cardioDate,
            "durationInMinutes" -> create.durationInMinutes,
            "caloriesBurned" -> create.caloriesBurned,
            "meetupId" -> create.meetupId,
            "now" -> now
          )
        )(cardioWorkoutRowParser).toDomain
      }
    }

  override def getAllStrengthWorkoutsForDayByUser(userId: UUID, day: Instant): Future[Seq[StrengthWorkout]] =
    Future {
      getRecords(SQL_GET_STRENGTH_WORKOUTS_FOR_USER_BY_DATE, "userId" -> userId, "exerciseDate" -> Date.from(day))(
        strengthWorkoutRowParser
      ).map(_.toDomain)
    }

  override def insertStrengthWorkoutForUser(
    id: UUID,
    userId: UUID,
    create: StrengthWorkout.Create
  ): Future[StrengthWorkout] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[StrengthWorkoutRow](
          SQL_INSERT_AND_RETURN_STRENGTH_WORKOUT(create.weightsInLbs),
          Seq(
            "id" -> id,
            "userId" -> userId,
            "workoutId" -> create.workoutId,
            "name" -> create.name,
            "exerciseDate" -> create.exerciseDate,
            "sets" -> create.sets,
            "reps" -> create.reps,
            "caloriesBurned" -> create.caloriesBurned,
            "meetupId" -> create.meetupId,
            "now" -> now
          )
        )(strengthWorkoutRowParser).toDomain
      }
    }

  override def deleteCardioWorkoutForUser(userId: UUID, id: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_CARDIO_WORKOUT_ENTRY, Seq("userId" -> userId, "cardioWorkoutEntryId" -> id))
    }

  override def deleteStrengthWorkoutForUser(userId: UUID, id: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(
        SQL_DELETE_STRENGTH_WORKOUT_ENTRY,
        Seq("userId" -> userId, "strengthWorkoutEntryId" -> id)
      )
    }
}

object AnormExerciseDiaryRepository extends AnormOps {

  private def transformWeightsIntoSqlArray(weightsInLbs: Seq[Long]): String = {
    if (weightsInLbs.nonEmpty) {
      weightsInLbs.foldLeft("ARRAY[")((acc, w) => acc + s"'$w'::uuid,").dropRight(1) + "]"
    } else {
      "ARRAY[".concat("]::integer[]")
    }

  }

  private def SQL_INSERT_AND_RETURN_STRENGTH_WORKOUT(weightsInLbs: Seq[Long]): String =
    s"""
      |insert into strength_workouts (id, user_id, workout_id, name, exercise_date, sets, reps, weight_in_lbs, calories_burned, meetup_id, created_at, updated_at)
      |values ({id}::uuid, {userId}::uuid, {workoutId}::uuid, {name}, {exerciseDate}, {sets}, {reps}, ${transformWeightsIntoSqlArray(
      weightsInLbs
    )}, {caloriesBurned}, {meetupId}::uuid, {now}, {now})
      |returning *;
      |""".stripMargin

  private val SQL_GET_STRENGTH_WORKOUTS_FOR_USER_BY_DATE: String =
    """
      |select * 
      |from strength_workouts
      |where user_id = {userId}::uuid
      |and exercise_date::date = {exerciseDate}::date ;
      |""".stripMargin

  private val SQL_INSERT_AND_RETURN_CARDIO_WORKOUT: String =
    """
      |insert into cardio_workouts (id, user_id, workout_id, name, cardio_date, duration_in_minutes, calories_burned, meetup_id, created_at, updated_at)
      |values ({id}::uuid, {userId}::uuid, {workoutId}::uuid, {name}, {cardioDate}, {durationInMinutes}, {caloriesBurned}, {meetupId}::uuid, {now}, {now})
      |returning *;
      |""".stripMargin

  private val SQL_GET_CARDIO_WORKOUTS_FOR_USER_BY_DATE: String =
    """
      |select * 
      |from cardio_workouts
      |where user_id = {userId}::uuid
      |and cardio_date::date = {cardioDate}::date ;
      |""".stripMargin

  private val SQL_DELETE_CARDIO_WORKOUT_ENTRY: String =
    """
      |delete from cardio_workouts
      |where user_id = {userId}::uuid 
      |and id = {cardioWorkoutEntryId}::uuid ;
      |""".stripMargin

  private val SQL_DELETE_ALL_CARDIO_WORKOUT_ENTRIES: String =
    """
      |delete from cardio_workouts
      |where user_id = {userId}::uuid  ;
      |""".stripMargin

  private val SQL_DELETE_ALL_STRENGTH_WORKOUT_ENTRIES: String =
    """
      |delete from strength_workouts
      |where user_id = {userId}::uuid  ;
      |""".stripMargin

  private val SQL_DELETE_STRENGTH_WORKOUT_ENTRY: String =
    """
      |delete from strength_workouts
      |where user_id = {userId}::uuid 
      |and id = {strengthWorkoutEntryId}::uuid ;
      |""".stripMargin

  private val SQL_GET_USER_RECENTLY_VIEWED_WORKOUTS: String =
    """
      |select workout_id
      |from user_recently_viewed_workouts
      |where user_id = {userId}::uuid
      |order_by last_accessed desc ;
      |""".stripMargin

  private val SQL_DELETE_USER_RECENTLY_VIEWED_WORKOUTS: String =
    """
      |delete from user_recently_viewed_workouts
      |where user_id = {userId}::uuid 
      |and workout_id = {workoutId}::uuid ;
      |""".stripMargin

  private val SQL_ADD_USER_RECENTLY_VIEWED_WORKOUTS: String =
    """
      |insert into user_recently_viewed_workouts (user_id, workout_id, last_accessed, created_at, updated_at)
      |values ({userId}:: uuid, {workoutId}::uuid, {now}, {now}, {now}) ;
      |""".stripMargin

  private case class StrengthWorkoutRow(
    id: UUID,
    user_id: UUID,
    workout_id: UUID,
    name: String,
    exercise_date: Instant,
    sets: Option[Int],
    reps: Option[Int],
    weight_in_lbs: List[Int],
    calories_burned: Option[Double],
    meetup_id: Option[UUID],
    created_at: Instant,
    updated_at: Instant
  ) {
    def toDomain: StrengthWorkout =
      StrengthWorkout(
        id = id,
        userId = user_id,
        workoutId = workout_id,
        name = name,
        exerciseDate = exercise_date,
        sets = sets,
        reps = reps,
        weightsInLbs = weight_in_lbs,
        caloriesBurned = calories_burned,
        meetupId = meetup_id,
        createdAt = created_at,
        updatedAt = updated_at
      )
  }

  private case class CardioWorkoutRow(
    id: UUID,
    user_id: UUID,
    workout_id: UUID,
    name: String,
    cardio_date: Instant,
    duration_in_minutes: Option[Int],
    calories_burned: Option[Double],
    meetup_id: Option[UUID],
    created_at: Instant,
    updated_at: Instant
  ) {
    def toDomain: CardioWorkout =
      CardioWorkout(
        id = id,
        userId = user_id,
        workoutId = workout_id,
        name = name,
        cardioDate = cardio_date,
        durationInMinutes = duration_in_minutes,
        caloriesBurned = calories_burned,
        meetupId = meetup_id,
        createdAt = created_at,
        updatedAt = updated_at
      )
  }

  private val cardioWorkoutRowParser: RowParser[CardioWorkoutRow] = Macro.namedParser[CardioWorkoutRow]
  private val strengthWorkoutRowParser: RowParser[StrengthWorkoutRow] = Macro.namedParser[StrengthWorkoutRow]
}
