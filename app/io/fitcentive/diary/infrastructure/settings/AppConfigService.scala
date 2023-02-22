package io.fitcentive.diary.infrastructure.settings

import com.typesafe.config.Config
import io.fitcentive.sdk.config.{GcpConfig, JwtConfig, SecretConfig, ServerConfig}
import io.fitcentive.diary.domain.config.{EnvironmentConfig, WgerApiConfig}
import io.fitcentive.diary.services.SettingsService
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfigService @Inject() (config: Configuration) extends SettingsService {

  override def exerciseApiConfig: WgerApiConfig = WgerApiConfig.fromConfig(config.get[Config]("wger"))

  override def envConfig: EnvironmentConfig = EnvironmentConfig.fromConfig(config.get[Config]("environment"))

  override def secretConfig: SecretConfig = SecretConfig.fromConfig(config.get[Config]("services"))

  override def keycloakServerUrl: String = config.get[String]("keycloak.server-url")

  override def jwtConfig: JwtConfig = JwtConfig.apply(config.get[Config]("jwt"))

  override def authServiceConfig: ServerConfig = ServerConfig.fromConfig(config.get[Config]("services.auth-service"))

  override def gcpConfig: GcpConfig =
    GcpConfig(project = config.get[String]("gcp.project"))

}
