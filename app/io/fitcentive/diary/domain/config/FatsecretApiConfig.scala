package io.fitcentive.diary.domain.config

import com.typesafe.config.Config

import scala.concurrent.duration.{Duration, DurationInt}

case class FatsecretApiConfig(
  apiHost: String,
  authHost: String,
  clientId: String,
  clientSecret: String,
  authTokenCacheDuration: Duration = 30.minutes,
  authTokenCacheKey: String = "auth-token-cache-key"
)

object FatsecretApiConfig {
  def fromConfig(config: Config): FatsecretApiConfig =
    FatsecretApiConfig(
      config.getString("api-host"),
      config.getString("auth-host"),
      config.getString("client-id"),
      config.getString("client-secret")
    )
}
