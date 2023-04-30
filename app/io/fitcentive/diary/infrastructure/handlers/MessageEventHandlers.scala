package io.fitcentive.diary.infrastructure.handlers

import io.fitcentive.diary.api.ExerciseApi
import io.fitcentive.diary.domain.events.{EventHandlers, EventMessage, WarmWgerApiCacheEvent}

import scala.concurrent.{ExecutionContext, Future}

trait MessageEventHandlers extends EventHandlers {

  def exerciseApi: ExerciseApi
  implicit def executionContext: ExecutionContext

  override def handleEvent(event: EventMessage): Future[Unit] =
    event match {
      case event: WarmWgerApiCacheEvent => exerciseApi.getAllExerciseInfo.map(_ => ())
      case _                            => Future.failed(new Exception("Unrecognized event"))
    }
}
