package io.fitcentive.diary.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubTopicConfig

case class TopicsConfig(
  warmWgerApiCacheTopic: String,
  userStepDataUpdatedTopic: String,
  userDiaryEntryCreatedTopic: String
) extends PubSubTopicConfig {

  val topics: Seq[String] =
    Seq(warmWgerApiCacheTopic, userStepDataUpdatedTopic, userDiaryEntryCreatedTopic)

}

object TopicsConfig {
  def fromConfig(config: Config): TopicsConfig =
    TopicsConfig(
      config.getString("warm-wger-api-cache"),
      config.getString("user-step-data-updated"),
      config.getString("user-diary-entry-created"),
    )
}
