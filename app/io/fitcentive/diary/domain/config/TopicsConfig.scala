package io.fitcentive.diary.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubTopicConfig

case class TopicsConfig(
  warmWgerApiCacheTopic: String,
  userStepDataUpdatedTopic: String,
  userDiaryEntryCreatedTopic: String,
  userWeightUpdatedTopic: String,
  checkIfUsersNeedPromptToLogWeightTopic: String,
  checkIfUsersNeedPromptToLogDiaryEntriesTopic: String,
  promptUserToLogWeightTopic: String,
  promptUserToLogDiaryEntryTopic: String,
) extends PubSubTopicConfig {

  val topics: Seq[String] =
    Seq(
      warmWgerApiCacheTopic,
      userStepDataUpdatedTopic,
      userDiaryEntryCreatedTopic,
      userWeightUpdatedTopic,
      checkIfUsersNeedPromptToLogWeightTopic,
      checkIfUsersNeedPromptToLogDiaryEntriesTopic,
      promptUserToLogWeightTopic,
      promptUserToLogDiaryEntryTopic
    )

}

object TopicsConfig {
  def fromConfig(config: Config): TopicsConfig =
    TopicsConfig(
      config.getString("warm-wger-api-cache"),
      config.getString("user-step-data-updated"),
      config.getString("user-diary-entry-created"),
      config.getString("user-weight-updated"),
      config.getString("check-if-users-need-prompt-to-log-weight"),
      config.getString("check-if-users-need-prompt-to-log-diary-entries"),
      config.getString("prompt-user-to-log-weight"),
      config.getString("prompt-user-to-log-diary-entry"),
    )
}
