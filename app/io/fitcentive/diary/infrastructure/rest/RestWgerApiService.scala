package io.fitcentive.diary.infrastructure.rest

import play.api.http.Status
import play.api.libs.ws.WSClient
import io.fitcentive.diary.domain.config.WgerApiConfig
import io.fitcentive.diary.domain.errors.WgerApiError
import io.fitcentive.diary.domain.wger.ExerciseDefinition
import io.fitcentive.diary.services.{ExerciseService, SettingsService}
import io.fitcentive.sdk.error.DomainError

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RestWgerApiService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit ec: ExecutionContext)
  extends ExerciseService {

  import RestWgerApiService._

  val wgerConfig: WgerApiConfig = settingsService.exerciseApiConfig
  val baseUrl: String = s"${wgerConfig.host}/${wgerConfig.apiVersion}"

  override def getCompleteExerciseDetailedInfo: Future[Either[DomainError, Seq[ExerciseDefinition]]] =
    wsClient
      .url(s"$baseUrl/exerciseinfo?format=json&limit=$defaultMax")
      .get()
      .map { response =>
        response.status match {
          case Status.OK => Right((response.json \ "results").as[Seq[ExerciseDefinition]])
          case status    => Left(WgerApiError(s"Unexpected status from Wger API: $status"))
        }
      }

}

object RestWgerApiService {
  val defaultMax: Int = 2000;
}
