package io.fitcentive.diary.api

import cats.data.EitherT
import io.fitcentive.sdk.error.{DomainError, EntityNotFoundError}
import io.fitcentive.diary.domain.wger.ExerciseDefinition
import io.fitcentive.diary.services._

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExerciseApi @Inject() (exerciseService: ExerciseService)(implicit ec: ExecutionContext) {

  val ENGLISH = 2

  val defaultLimit = 50
  val defaultOffset = 0

  def getExerciseInfoForWorkoutId(workoutId: UUID): Future[Either[DomainError, ExerciseDefinition]] =
    (for {
      exerciseDefinitionOpt <-
        EitherT[Future, DomainError, Option[ExerciseDefinition]](exerciseService.getExerciseInfoForWorkoutId(workoutId))
      exerciseDefinition <- EitherT[Future, DomainError, ExerciseDefinition] {
        exerciseDefinitionOpt
          .map(e => Future.successful(Right.apply(e)))
          .getOrElse(
            Future.successful(Left(EntityNotFoundError(s"No exercise definition found for workoutId: $workoutId")))
          )
      }
    } yield exerciseDefinition).value

  def getAllExerciseInfo: Future[Either[DomainError, Seq[ExerciseDefinition]]] =
    exerciseService.getCompleteExerciseDetailedInfo
      .map(_.map(_.filter(_.language.id == ENGLISH)))

  implicit class OptionalStringToEmpty(s: Option[String]) {
    def optString: String =
      s.fold("")(identity)
  }

}
