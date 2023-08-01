package io.fitcentive.diary.infrastructure.pubsub

import io.fitcentive.diary.domain.config.TopicsConfig
import io.fitcentive.diary.domain.exercise.UserStepsData
import io.fitcentive.diary.infrastructure.contexts.PubSubExecutionContext
import io.fitcentive.diary.services.{MessageBusService, SettingsService}
import io.fitcentive.registry.events.diary.UserDiaryEntryCreated
import io.fitcentive.registry.events.steps.UserStepDataUpdated
import io.fitcentive.sdk.gcp.pubsub.PubSubPublisher

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class EventPublisherService @Inject() (publisher: PubSubPublisher, settingsService: SettingsService)(implicit
  ec: PubSubExecutionContext
) extends MessageBusService {

  private val publisherConfig: TopicsConfig = settingsService.pubSubConfig.topicsConfig

  override def publishUserStepDataUpdatedEvent(stepsData: UserStepsData): Future[Unit] =
    stepsData.toOut
      .pipe(publisher.publish(publisherConfig.userStepDataUpdatedTopic, _))

  override def publishUserDiaryEntryCreatedEvent(
    userId: UUID,
    entryDate: String,
    activityMinutes: Option[Int]
  ): Future[Unit] =
    UserDiaryEntryCreated(userId = userId, date = entryDate, activityMinutes = activityMinutes)
      .pipe(publisher.publish(publisherConfig.userDiaryEntryCreatedTopic, _))

  implicit class UserStepsDataToOut(in: UserStepsData) {
    def toOut: UserStepDataUpdated =
      UserStepDataUpdated(userId = in.userId, date = in.entryDate, stepsTaken = in.steps)
  }
}
