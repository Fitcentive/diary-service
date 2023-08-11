package io.fitcentive.diary.services

import com.google.inject.ImplementedBy
import io.fitcentive.diary.domain.exercise.UserStepsData
import io.fitcentive.diary.infrastructure.pubsub.EventPublisherService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[EventPublisherService])
trait MessageBusService {
  def publishUserStepDataUpdatedEvent(stepsData: UserStepsData): Future[Unit]
  def publishUserDiaryEntryCreatedEvent(userId: UUID, entryDate: String, activityMinutes: Option[Int]): Future[Unit]
  def publishUserWeightUpdatedEvent(userId: UUID, entryDate: String, newWeightInLbs: Double): Future[Unit]
  def publishNotifyUserToPromptForWeightEntry(userId: UUID): Future[Unit]
  def publishNotifyUserToPromptForDiaryEntry(userId: UUID): Future[Unit]
}
