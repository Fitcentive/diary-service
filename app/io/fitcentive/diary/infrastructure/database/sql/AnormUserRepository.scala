package io.fitcentive.diary.infrastructure.database.sql

import anorm.{Macro, RowParser}
import io.fitcentive.diary.domain.user.{FitnessUserProfile, UserFitnessActivityLevel, UserFitnessGoal}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.sdk.utils.AnormOps
import io.fitcentive.diary.repositories.UserRepository
import play.api.db.Database

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class AnormUserRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends UserRepository
  with DatabaseClient {

  import AnormUserRepository._

  override def deleteFitnessUserProfile(userId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_FITNESS_USER_PROFILE_BY_USER, Seq("userId" -> userId))
    }

  override def getFitnessUserProfile(userId: UUID): Future[Option[FitnessUserProfile]] =
    Future {
      getRecordOpt(SQL_GET_FITNESS_USER_PROFILE_BY_USER, "userId" -> userId)(fitnessUserProfileRowParser)
        .map(_.toDomain)
    }

  override def upsertFitnessUserProfile(
    userId: UUID,
    fitnessUserProfileUpdate: FitnessUserProfile.Update
  ): Future[FitnessUserProfile] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[FitnessUserProfileRow](
          SQL_UPSERT_FITNESS_USER_PROFILE_AND_RETURN,
          Seq(
            "userId" -> userId,
            "heightInCm" -> fitnessUserProfileUpdate.heightInCm,
            "weightInLbs" -> fitnessUserProfileUpdate.weightInLbs,
            "now" -> now,
            "activityLevel" -> UserFitnessActivityLevel(fitnessUserProfileUpdate.activityLevel).stringValue,
            "goal" -> UserFitnessGoal(fitnessUserProfileUpdate.goal).stringValue,
            "stepGoalPerDay" -> fitnessUserProfileUpdate.stepGoalPerDay,
            "goalWeightInLbs" -> fitnessUserProfileUpdate.goalWeightInLbs,
          )
        )(fitnessUserProfileRowParser).toDomain
      }
    }
}

object AnormUserRepository extends AnormOps {

  private val SQL_DELETE_FITNESS_USER_PROFILE_BY_USER: String =
    """
      |delete from fitness_user_profile
      |where user_id = {userId}::uuid ;
      |""".stripMargin

  private val SQL_UPSERT_FITNESS_USER_PROFILE_AND_RETURN: String =
    """
      |insert into fitness_user_profile (user_id, height_in_cm, weight_in_lbs, created_at, updated_at, activity_level, goal, step_goal_per_day, goal_weight_in_lbs)
      |values ({userId}::uuid, {heightInCm}, {weightInLbs}, {now}, {now}, {activityLevel}, {goal}, {stepGoalPerDay}, {goalWeightInLbs})
      |on conflict (user_id)
      |do update set
      |  weight_in_lbs = {weightInLbs},
      |  height_in_cm = {heightInCm},
      |  activity_level = {activityLevel},
      |  goal = {goal},
      |  step_goal_per_day = {stepGoalPerDay},
      |  goal_weight_in_lbs = {goalWeightInLbs},
      |  updated_at = {now}
      |returning *;
      |""".stripMargin

  private val SQL_GET_FITNESS_USER_PROFILE_BY_USER: String =
    """
      |select * 
      |from fitness_user_profile
      |where user_id = {userId}::uuid ;
      |""".stripMargin

  private case class FitnessUserProfileRow(
    user_id: UUID,
    height_in_cm: Double,
    weight_in_lbs: Double,
    activity_level: String,
    goal: String,
    step_goal_per_day: Option[Int],
    goal_weight_in_lbs: Option[Double],
    created_at: Instant,
    updated_at: Instant
  ) {
    def toDomain: FitnessUserProfile =
      FitnessUserProfile(
        userId = user_id,
        heightInCm = height_in_cm,
        weightInLbs = weight_in_lbs,
        goalWeightInLbs = goal_weight_in_lbs,
        activityLevel = UserFitnessActivityLevel(activity_level),
        goal = UserFitnessGoal(goal),
        stepGoalPerDay = step_goal_per_day,
        createdAt = created_at,
        updatedAt = updated_at
      )
  }

  private val fitnessUserProfileRowParser: RowParser[FitnessUserProfileRow] = Macro.namedParser[FitnessUserProfileRow]
}
