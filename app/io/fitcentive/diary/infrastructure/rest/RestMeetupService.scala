package io.fitcentive.diary.infrastructure.rest

import io.fitcentive.diary.domain.errors.MeetupServiceError
import io.fitcentive.diary.infrastructure.utils.ServiceSecretSupport
import io.fitcentive.diary.services.{MeetupService, SettingsService}
import io.fitcentive.sdk.config.ServerConfig
import io.fitcentive.sdk.error.DomainError
import play.api.http.Status
import play.api.libs.ws.WSClient

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RestMeetupService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit ec: ExecutionContext)
  extends MeetupService
  with ServiceSecretSupport {

  val meetupServiceConfig: ServerConfig = settingsService.meetupServiceConfig
  val baseUrl: String = meetupServiceConfig.serverUrl

  override def deleteCardioEntryAssociatedToMeetup(diaryEntryId: UUID): Future[Either[DomainError, Unit]] =
    wsClient
      .url(s"$baseUrl/api/internal/meetup/cardio-entry/$diaryEntryId")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .delete()
      .map { response =>
        response.status match {
          case Status.OK => Right()
          case status    => Left(MeetupServiceError(s"Unexpected status from meetup-service: $status"))
        }
      }

  override def deleteStrengthEntryAssociatedToMeetup(diaryEntryId: UUID): Future[Either[DomainError, Unit]] =
    wsClient
      .url(s"$baseUrl/api/internal/meetup/strength-entry/$diaryEntryId")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .delete()
      .map { response =>
        response.status match {
          case Status.OK => Right()
          case status    => Left(MeetupServiceError(s"Unexpected status from meetup-service: $status"))
        }
      }

  override def deleteFoodEntryAssociatedToMeetup(diaryEntryId: UUID): Future[Either[DomainError, Unit]] =
    wsClient
      .url(s"$baseUrl/api/internal/meetup/food-entry/$diaryEntryId")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .delete()
      .map { response =>
        response.status match {
          case Status.OK => Right()
          case status    => Left(MeetupServiceError(s"Unexpected status from meetup-service: $status"))
        }
      }
}
