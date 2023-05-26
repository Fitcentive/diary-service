package io.fitcentive.diary.domain.config

import com.typesafe.config.Config

case class WgerApiConfig(host: String, apiVersion: String, allExercisesCacheKey: String = "all-exercises-cache-key")

object WgerApiConfig {
  def fromConfig(config: Config): WgerApiConfig =
    WgerApiConfig(config.getString("host"), config.getString("api-version"))
}
