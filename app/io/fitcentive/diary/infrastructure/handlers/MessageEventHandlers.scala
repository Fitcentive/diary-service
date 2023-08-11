package io.fitcentive.diary.infrastructure.handlers

import io.fitcentive.diary.api.ExerciseApi
import io.fitcentive.diary.domain.events.{
  CheckIfUsersNeedPromptToLogDiaryEntriesEvent,
  CheckIfUsersNeedPromptToLogWeightEvent,
  EventHandlers,
  EventMessage,
  WarmWgerApiCacheEvent
}

import scala.concurrent.{ExecutionContext, Future}

trait MessageEventHandlers extends EventHandlers {

  def exerciseApi: ExerciseApi
  implicit def executionContext: ExecutionContext

  override def handleEvent(event: EventMessage): Future[Unit] =
    event match {
      case event: WarmWgerApiCacheEvent =>
        exerciseApi.getAllExerciseInfo.map(_ => ())

      case event: CheckIfUsersNeedPromptToLogDiaryEntriesEvent =>
        exerciseApi.checkIfUsersNeedPromptToLogDiaryEntriesEvent(event.userIds).map(_ => ())

      case event: CheckIfUsersNeedPromptToLogWeightEvent =>
        exerciseApi.checkIfUsersNeedPromptToLogWeightEvent(event.userIds).map(_ => ())

      case _ => Future.failed(new Exception("Unrecognized event"))
    }
}
