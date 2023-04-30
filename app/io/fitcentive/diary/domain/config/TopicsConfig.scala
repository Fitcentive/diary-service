package io.fitcentive.diary.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubTopicConfig

case class TopicsConfig(warmWgerApiCacheTopic: String) extends PubSubTopicConfig {

  val topics: Seq[String] =
    Seq(warmWgerApiCacheTopic)

}

object TopicsConfig {
  def fromConfig(config: Config): TopicsConfig =
    TopicsConfig(config.getString("warm-wger-api-cache"))
}
