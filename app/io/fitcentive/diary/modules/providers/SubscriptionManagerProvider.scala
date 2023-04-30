package io.fitcentive.diary.modules.providers

import com.google.auth.oauth2.ServiceAccountCredentials
import io.fitcentive.diary.api.ExerciseApi
import io.fitcentive.diary.infrastructure.contexts.PubSubExecutionContext
import io.fitcentive.diary.infrastructure.handlers.MessageEventHandlers
import io.fitcentive.diary.infrastructure.pubsub.SubscriptionManager
import io.fitcentive.diary.services.SettingsService
import io.fitcentive.sdk.gcp.pubsub.{PubSubPublisher, PubSubSubscriber}

import java.io.ByteArrayInputStream
import javax.inject.{Inject, Provider}
import scala.concurrent.ExecutionContext

class SubscriptionManagerProvider @Inject() (
  publisher: PubSubPublisher,
  settingsService: SettingsService,
  _exerciseApi: ExerciseApi
)(implicit ec: PubSubExecutionContext)
  extends Provider[SubscriptionManager] {

  trait SubscriptionEventHandlers extends MessageEventHandlers {
    override def exerciseApi: ExerciseApi = _exerciseApi
    override implicit def executionContext: ExecutionContext = ec
  }

  override def get(): SubscriptionManager = {
    val credentials =
      ServiceAccountCredentials
        .fromStream(new ByteArrayInputStream(settingsService.serviceAccountStringCredentials.getBytes()))
        .createScoped()
    new SubscriptionManager(
      publisher = publisher,
      subscriber = new PubSubSubscriber(credentials, settingsService.gcpConfig.project),
      config = settingsService.pubSubConfig,
      environment = settingsService.envConfig.environment
    ) with SubscriptionEventHandlers
  }
}
