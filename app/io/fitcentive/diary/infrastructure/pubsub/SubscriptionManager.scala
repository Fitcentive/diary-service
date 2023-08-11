package io.fitcentive.diary.infrastructure.pubsub

import io.fitcentive.diary.domain.config.AppPubSubConfig
import io.fitcentive.diary.domain.events.{
  CheckIfUsersNeedPromptToLogDiaryEntriesEvent,
  CheckIfUsersNeedPromptToLogWeightEvent,
  EventHandlers,
  WarmWgerApiCacheEvent
}
import io.fitcentive.diary.infrastructure.contexts.PubSubExecutionContext
import io.fitcentive.sdk.gcp.pubsub.{PubSubPublisher, PubSubSubscriber}
import io.fitcentive.sdk.logging.AppLogger

import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

class SubscriptionManager(
  publisher: PubSubPublisher,
  subscriber: PubSubSubscriber,
  config: AppPubSubConfig,
  environment: String
)(implicit ec: PubSubExecutionContext)
  extends AppLogger {

  self: EventHandlers =>

  initializeSubscriptions

  final def initializeSubscriptions: Future[Unit] = {
    for {
      _ <- Future.sequence(config.topicsConfig.topics.map(publisher.createTopic))
      _ <- subscribeToWgerCacheEvent
      _ <- subscribeToCheckIfUsersNeedPromptToLogWeightEvent
      _ <- subscribeToCheckIfUsersNeedPromptToLogDiaryEntriesEvent

      _ = logInfo("Subscriptions set up successfully!")
    } yield ()
  }

  def subscribeToCheckIfUsersNeedPromptToLogDiaryEntriesEvent: Future[Unit] =
    subscriber
      .subscribe[CheckIfUsersNeedPromptToLogDiaryEntriesEvent](
        environment,
        config.subscriptionsConfig.checkIfUsersNeedPromptToLogDiaryEntriesSubscription,
        config.topicsConfig.checkIfUsersNeedPromptToLogDiaryEntriesTopic
      )(_.payload.pipe(handleEvent))

  def subscribeToCheckIfUsersNeedPromptToLogWeightEvent: Future[Unit] =
    subscriber
      .subscribe[CheckIfUsersNeedPromptToLogWeightEvent](
        environment,
        config.subscriptionsConfig.checkIfUsersNeedPromptToLogWeightSubscription,
        config.topicsConfig.checkIfUsersNeedPromptToLogWeightTopic
      )(_.payload.pipe(handleEvent))

  def subscribeToWgerCacheEvent: Future[Unit] =
    subscriber
      .subscribe[WarmWgerApiCacheEvent](
        environment,
        config.subscriptionsConfig.warmWgerApiCacheSubscription,
        config.topicsConfig.warmWgerApiCacheTopic
      )(_.payload.pipe(handleEvent))
}
