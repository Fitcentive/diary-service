package io.fitcentive.diary.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.config.{GcpConfig, JwtConfig, SecretConfig, ServerConfig}
import io.fitcentive.diary.domain.config.{EnvironmentConfig, WgerApiConfig}
import io.fitcentive.diary.infrastructure.settings.AppConfigService

@ImplementedBy(classOf[AppConfigService])
trait SettingsService {
  def gcpConfig: GcpConfig
  def authServiceConfig: ServerConfig
  def jwtConfig: JwtConfig
  def keycloakServerUrl: String
  def secretConfig: SecretConfig
  def envConfig: EnvironmentConfig
  def exerciseApiConfig: WgerApiConfig
}
