package io.fitcentive.diary.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubSubscriptionConfig

case class SubscriptionsConfig(
  warmWgerApiCacheSubscription: String,
  checkIfUsersNeedPromptToLogWeightSubscription: String
) extends PubSubSubscriptionConfig {
  val subscriptions: Seq[String] = Seq(warmWgerApiCacheSubscription, checkIfUsersNeedPromptToLogWeightSubscription)
}

object SubscriptionsConfig {
  def fromConfig(config: Config): SubscriptionsConfig =
    SubscriptionsConfig(
      config.getString("warm-wger-api-cache"),
      config.getString("check-if-users-need-prompt-to-log-weight"),
    )
}
