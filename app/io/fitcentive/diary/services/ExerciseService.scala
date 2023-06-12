package io.fitcentive.diary.services

import com.google.inject.ImplementedBy
import io.fitcentive.diary.domain.wger.ExerciseDefinition
import io.fitcentive.diary.infrastructure.rest.RestWgerApiService
import io.fitcentive.sdk.error.DomainError

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestWgerApiService])
trait ExerciseService {
  def getCompleteExerciseDetailedInfo: Future[Either[DomainError, Seq[ExerciseDefinition]]]
  def getExerciseInfoForWorkoutId(workoutId: UUID): Future[Either[DomainError, Option[ExerciseDefinition]]]
}
