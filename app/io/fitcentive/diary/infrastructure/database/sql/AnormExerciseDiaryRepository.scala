package io.fitcentive.diary.infrastructure.database.sql

import anorm.{Macro, RowParser, SqlParser}
import io.fitcentive.diary.domain.exercise.{CardioWorkout, StrengthWorkout, UserStepsData}
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

  override def upsertMostRecentlyViewedWorkoutForUser(userId: UUID, workoutId: UUID): Future[Unit] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithoutReturning(
          SQL_UPSERT_USER_RECENTLY_VIEWED_WORKOUTS,
          Seq("id" -> UUID.randomUUID(), "userId" -> userId, "workoutId" -> workoutId, "now" -> now)
        )
      }
    }

  override def associateMeetupWithCardioEntryById(cardioEntryId: UUID, meetupId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(
        SQL_ASSOCIATE_MEETUP_FROM_CARDIO_ENTRY,
        Seq("cardioEntryId" -> cardioEntryId, "meetupId" -> meetupId)
      )
    }

  override def associateMeetupWithStrengthEntryById(strengthEntryId: UUID, meetupId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(
        SQL_ASSOCIATE_MEETUP_FROM_STRENGTH_ENTRY,
        Seq("strengthEntryId" -> strengthEntryId, "meetupId" -> meetupId)
      )
    }

  override def dissociateMeetupFromCardioEntryById(cardioEntryId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DISSOCIATE_MEETUP_FROM_CARDIO_ENTRY, Seq("cardioEntryId" -> cardioEntryId))
    }

  override def dissociateMeetupFromStrengthEntryById(strengthEntryId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DISSOCIATE_MEETUP_FROM_STRENGTH_ENTRY, Seq("strengthEntryId" -> strengthEntryId))
    }

  override def deleteAllCardioWorkoutsForUser(userId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_ALL_CARDIO_WORKOUT_ENTRIES, Seq("userId" -> userId))
    }

  override def deleteAllStrengthWorkoutsForUser(userId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_ALL_STRENGTH_WORKOUT_ENTRIES, Seq("userId" -> userId))
    }

  override def updateCardioWorkoutByIdForUser(
    userId: UUID,
    cardioDiaryEntryId: UUID,
    update: CardioWorkout.Update
  ): Future[CardioWorkout] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[CardioWorkoutRow](
          SQL_UPDATE_CARDIO_WORKOUT_FOR_USER_BY_ID,
          Seq(
            "id" -> cardioDiaryEntryId,
            "userId" -> userId,
            "cardioDate" -> update.cardioDate,
            "durationInMinutes" -> update.durationInMinutes,
            "caloriesBurned" -> update.caloriesBurned,
            "meetupId" -> update.meetupId,
            "now" -> now
          )
        )(cardioWorkoutRowParser).toDomain
      }
    }

  override def updateStrengthWorkoutByIdForUser(
    userId: UUID,
    strengthDiaryEntryId: UUID,
    update: StrengthWorkout.Update
  ): Future[StrengthWorkout] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[StrengthWorkoutRow](
          SQL_UPDATE_AND_RETURN_STRENGTH_WORKOUT_FOR_USER_BY_ID(update.weightsInLbs),
          Seq(
            "id" -> strengthDiaryEntryId,
            "userId" -> userId,
            "exerciseDate" -> update.exerciseDate,
            "sets" -> update.sets,
            "reps" -> update.reps,
            "caloriesBurned" -> update.caloriesBurned,
            "meetupId" -> update.meetupId,
            "now" -> now
          )
        )(strengthWorkoutRowParser).toDomain
      }
    }

  override def getCardioWorkoutByIdForUser(userId: UUID, cardioDiaryEntryId: UUID): Future[Option[CardioWorkout]] =
    Future {
      getRecordOpt(SQL_GET_CARDIO_WORKOUT_FOR_USER_BY_ID, "userId" -> userId, "diaryEntryId" -> cardioDiaryEntryId)(
        cardioWorkoutRowParser
      ).map(_.toDomain)
    }

  override def getStrengthWorkoutByIdForUser(
    userId: UUID,
    strengthDiaryEntryId: UUID
  ): Future[Option[StrengthWorkout]] =
    Future {
      getRecordOpt(SQL_GET_STRENGTH_WORKOUT_FOR_USER_BY_ID, "userId" -> userId, "diaryEntryId" -> strengthDiaryEntryId)(
        strengthWorkoutRowParser
      ).map(_.toDomain)
    }

  override def getCountOfCardioWorkoutsForDayByUser(
    userId: UUID,
    windowStart: Instant,
    windowEnd: Instant
  ): Future[Int] =
    Future {
      executeSqlWithExpectedReturn(
        SQL_GET_CARDIO_WORKOUTS_COUNT_FOR_USER_BY_DATE,
        Seq("userId" -> userId, "windowStart" -> windowStart, "windowEnd" -> windowEnd)
      )(SqlParser.scalar[Int])
    }

  override def getAllCardioWorkoutsForDayByUser(
    userId: UUID,
    windowStart: Instant,
    windowEnd: Instant
  ): Future[Seq[CardioWorkout]] =
    Future {
      getRecords(
        SQL_GET_CARDIO_WORKOUTS_FOR_USER_BY_DATE,
        "userId" -> userId,
        "windowStart" -> windowStart,
        "windowEnd" -> windowEnd
      )(cardioWorkoutRowParser).map(_.toDomain)
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

  override def getCountOfStrengthWorkoutsForDayByUser(
    userId: UUID,
    windowStart: Instant,
    windowEnd: Instant
  ): Future[Int] =
    Future {
      executeSqlWithExpectedReturn(
        SQL_GET_STRENGTH_WORKOUTS_COUNT_FOR_USER_BY_DATE,
        Seq("userId" -> userId, "windowStart" -> windowStart, "windowEnd" -> windowEnd)
      )(SqlParser.scalar[Int])
    }

  override def getAllStrengthWorkoutsForDayByUser(
    userId: UUID,
    windowStart: Instant,
    windowEnd: Instant
  ): Future[Seq[StrengthWorkout]] =
    Future {
      getRecords(
        SQL_GET_STRENGTH_WORKOUTS_FOR_USER_BY_DATE,
        "userId" -> userId,
        "windowStart" -> windowStart,
        "windowEnd" -> windowEnd
      )(strengthWorkoutRowParser).map(_.toDomain)
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

  override def getCardioWorkoutsById(cardioDiaryEntryIds: Seq[UUID]): Future[Seq[CardioWorkout]] =
    Future {
      getRecords(SQL_GET_CARDIO_WORKOUTS_BY_ID(cardioDiaryEntryIds))(cardioWorkoutRowParser)
        .map(_.toDomain)
    }

  override def getStrengthWorkoutsByIds(strengthDiaryEntryIds: Seq[UUID]): Future[Seq[StrengthWorkout]] =
    Future {
      getRecords(SQL_GET_STRENGTH_WORKOUTS_BY_ID(strengthDiaryEntryIds))(strengthWorkoutRowParser).map(_.toDomain)
    }

  override def upsertUserStepsData(
    userId: UUID,
    stepsTaken: Int,
    caloriesBurned: Double,
    entryDate: String
  ): Future[UserStepsData] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn(
          SQL_UPSERT_AND_RETURN_STEPS,
          Seq(
            "id" -> UUID.randomUUID(),
            "userId" -> userId,
            "steps" -> stepsTaken,
            "caloriesBurned" -> caloriesBurned,
            "entryDate" -> entryDate,
            "now" -> now
          )
        )(stepsRowParser).toDomain
      }
    }

  override def getUserStepsData(userId: UUID, dateString: String): Future[Option[UserStepsData]] =
    Future {
      getRecordOpt(SQL_GET_USER_STEPS, "userId" -> userId, "dateString" -> dateString)(stepsRowParser).map(_.toDomain)
    }

  override def deleteAllUserStepsData(userId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_ALL_USER_STEP_DATA, Seq("userId" -> userId))
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

  private def SQL_UPDATE_AND_RETURN_STRENGTH_WORKOUT_FOR_USER_BY_ID(weightsInLbs: Seq[Long]): String =
    s"""
       |update strength_workouts
       |set 
       | exercise_date = {exerciseDate},
       | sets = {sets},
       | reps = {reps},
       | weight_in_lbs = ${transformWeightsIntoSqlArray(weightsInLbs)},
       | calories_burned = {caloriesBurned},
       | meetup_id = {meetupId}::uuid,
       | updated_at = {now}
       |where user_id = {userId}::uuid
       |and id = {id}::uuid
       |returning * ;
       |""".stripMargin

  private val SQL_GET_STRENGTH_WORKOUTS_FOR_USER_BY_DATE: String =
    """
      |select * 
      |from strength_workouts
      |where user_id = {userId}::uuid
      |and exercise_date >= {windowStart}
      |and exercise_date <= {windowEnd} ;
      |""".stripMargin

  private val SQL_GET_STRENGTH_WORKOUTS_COUNT_FOR_USER_BY_DATE: String =
    """
      |select count(*)
      |from strength_workouts
      |where user_id = {userId}::uuid
      |and exercise_date >= {windowStart}
      |and exercise_date <= {windowEnd} ;
      |""".stripMargin

  private val SQL_DELETE_ALL_USER_STEP_DATA: String =
    """
      |delete from steps_taken
      |where user_id = {userId}::uuid ;
      |""".stripMargin

  private val SQL_UPSERT_AND_RETURN_STEPS: String =
    """
      |insert into steps_taken (id, user_id, steps, calories_burned, entry_date, created_at, updated_at)
      |values ({id}::uuid, {userId}::uuid, {steps}, {caloriesBurned}, {entryDate}, {now}, {now})
      |on conflict (user_id, entry_date)
      |do update set
      | steps = {steps},
      | calories_burned = {caloriesBurned},
      | updated_at = {now}
      |returning * ;
      |""".stripMargin

  private val SQL_GET_USER_STEPS: String =
    """
      |select *
      |from steps_taken
      |where user_id = {userId}::uuid
      |and entry_date = {dateString} ;
      |""".stripMargin

  private val SQL_INSERT_AND_RETURN_CARDIO_WORKOUT: String =
    """
      |insert into cardio_workouts (id, user_id, workout_id, name, cardio_date, duration_in_minutes, calories_burned, meetup_id, created_at, updated_at)
      |values ({id}::uuid, {userId}::uuid, {workoutId}::uuid, {name}, {cardioDate}, {durationInMinutes}, {caloriesBurned}, {meetupId}::uuid, {now}, {now})
      |returning *;
      |""".stripMargin

  private val SQL_GET_CARDIO_WORKOUTS_COUNT_FOR_USER_BY_DATE: String =
    """
    |select count(*)
    |from cardio_workouts
    |where user_id = {userId}::uuid
    |and cardio_date >= {windowStart} 
    |and cardio_date <= {windowEnd} ;
      |""".stripMargin

  private val SQL_GET_CARDIO_WORKOUTS_FOR_USER_BY_DATE: String =
    """
      |select * 
      |from cardio_workouts
      |where user_id = {userId}::uuid
      |and cardio_date >= {windowStart} 
      |and cardio_date <= {windowEnd} ;
      |""".stripMargin

  private val SQL_DELETE_CARDIO_WORKOUT_ENTRY: String =
    """
      |delete from cardio_workouts
      |where user_id = {userId}::uuid 
      |and id = {cardioWorkoutEntryId}::uuid ;
      |""".stripMargin

  private val SQL_ASSOCIATE_MEETUP_FROM_CARDIO_ENTRY: String =
    """
      |update cardio_workouts
      |set meetup_id = {meetupId}::uuid
      |where id = {cardioEntryId}::uuid ;
      |""".stripMargin

  private val SQL_DISSOCIATE_MEETUP_FROM_CARDIO_ENTRY: String =
    """
      |update cardio_workouts
      |set meetup_id = null
      |where id = {cardioEntryId}::uuid ;
      |""".stripMargin

  private val SQL_DISSOCIATE_MEETUP_FROM_STRENGTH_ENTRY: String =
    """
      |update strength_workouts
      |set meetup_id = null
      |where id = {strengthEntryId}::uuid ;
      |""".stripMargin

  private val SQL_ASSOCIATE_MEETUP_FROM_STRENGTH_ENTRY: String =
    """
      |update strength_workouts
      |set meetup_id = {meetupId}::uuid
      |where id = {strengthEntryId}::uuid ;
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
      |order by last_accessed desc ;
      |""".stripMargin

  private val SQL_UPDATE_CARDIO_WORKOUT_FOR_USER_BY_ID: String =
    """
      |update cardio_workouts
      |set 
      | cardio_date = {cardioDate},
      | duration_in_minutes = {durationInMinutes},
      | calories_burned = {caloriesBurned},
      | meetup_id = {meetupId}::uuid,
      | updated_at = {now}
      |where user_id = {userId}::uuid
      |and id = {id}::uuid 
      |returning * ;
      |""".stripMargin

  private val SQL_GET_CARDIO_WORKOUT_FOR_USER_BY_ID: String =
    """
      |select *
      |from cardio_workouts
      |where user_id = {userId}::uuid
      |and id = {diaryEntryId}::uuid ;
      |""".stripMargin

  private def SQL_GET_CARDIO_WORKOUTS_BY_ID(ids: Seq[UUID]): String = {
    val sql = """
                |select *
                |from cardio_workouts
                |where id in (
                |""".stripMargin
    transformUuidsToSql(ids, sql)
  }

  private def SQL_GET_STRENGTH_WORKOUTS_BY_ID(ids: Seq[UUID]): String = {
    val sql = """
      |select *
      |from strength_workouts
      |where id in (
      |""".stripMargin
    transformUuidsToSql(ids, sql)
  }

  private val SQL_GET_STRENGTH_WORKOUT_FOR_USER_BY_ID: String =
    """
      |select *
      |from strength_workouts
      |where user_id = {userId}::uuid
      |and id = {diaryEntryId}::uuid ;
      |""".stripMargin

  private val SQL_DELETE_USER_RECENTLY_VIEWED_WORKOUTS: String =
    """
      |delete from user_recently_viewed_workouts
      |where user_id = {userId}::uuid 
      |and workout_id = {workoutId}::uuid ;
      |""".stripMargin

  private val SQL_UPSERT_USER_RECENTLY_VIEWED_WORKOUTS: String =
    """
      |insert into user_recently_viewed_workouts (id, user_id, workout_id, last_accessed, created_at, updated_at)
      |values ({id}::uuid, {userId}:: uuid, {workoutId}::uuid, {now}, {now}, {now}) 
      |on conflict (user_id, workout_id) 
      |do update set 
      |last_accessed = {now};
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

  private case class StepsRow(
    id: UUID,
    user_id: UUID,
    steps: Int,
    calories_burned: Double,
    entry_date: String,
    created_at: Instant,
    updated_at: Instant,
  ) {
    def toDomain: UserStepsData =
      UserStepsData(
        id = id,
        userId = user_id,
        steps = steps,
        caloriesBurned = calories_burned,
        entryDate = entry_date,
        createdAt = created_at,
        updatedAt = updated_at,
      )
  }

  private val cardioWorkoutRowParser: RowParser[CardioWorkoutRow] = Macro.namedParser[CardioWorkoutRow]
  private val strengthWorkoutRowParser: RowParser[StrengthWorkoutRow] = Macro.namedParser[StrengthWorkoutRow]
  private val stepsRowParser: RowParser[StepsRow] = Macro.namedParser[StepsRow]
}
