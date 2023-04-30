package io.fitcentive.diary.modules

import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.inject.{AbstractModule, Provides}
import io.fitcentive.diary.infrastructure.pubsub.SubscriptionManager
import io.fitcentive.diary.modules.providers.SubscriptionManagerProvider
import io.fitcentive.diary.services.SettingsService
import io.fitcentive.sdk.gcp.pubsub.PubSubPublisher

import java.io.ByteArrayInputStream
import javax.inject.Singleton

class PubSubModule extends AbstractModule {

  @Provides
  @Singleton
  def providePubSubPublisher(settingsService: SettingsService): PubSubPublisher = {
    val credentials =
      ServiceAccountCredentials
        .fromStream(new ByteArrayInputStream(settingsService.serviceAccountStringCredentials.getBytes()))
        .createScoped()
    new PubSubPublisher(credentials, settingsService.gcpConfig.project)
  }

  override def configure(): Unit = {
    bind(classOf[SubscriptionManager]).toProvider(classOf[SubscriptionManagerProvider]).asEagerSingleton()
  }

}
