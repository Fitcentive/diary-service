package io.fitcentive.diary.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubSubscriptionConfig

case class SubscriptionsConfig(warmWgerApiCacheSubscription: String) extends PubSubSubscriptionConfig {
  val subscriptions: Seq[String] = Seq(warmWgerApiCacheSubscription)
}

object SubscriptionsConfig {
  def fromConfig(config: Config): SubscriptionsConfig =
    SubscriptionsConfig(config.getString("warm-wger-api-cache"))
}
