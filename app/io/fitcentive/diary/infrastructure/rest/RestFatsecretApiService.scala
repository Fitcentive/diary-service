package io.fitcentive.diary.infrastructure.rest

import play.api.http.Status
import play.api.libs.ws.{EmptyBody, WSAuthScheme, WSClient}
import io.fitcentive.diary.domain.config.FatsecretApiConfig
import io.fitcentive.diary.domain.errors.FatsecretApiError
import io.fitcentive.diary.domain.fatsecret.{
  FoodGetResult,
  FoodGetResultSingleServing,
  FoodSearchResults,
  FoodSearchSuggestions
}
import io.fitcentive.diary.services.{NutritionService, SettingsService}
import io.fitcentive.sdk.error.DomainError
import play.api.cache.AsyncCacheApi

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class RestFatsecretApiService @Inject() (wsClient: WSClient, cache: AsyncCacheApi, settingsService: SettingsService)(
  implicit ec: ExecutionContext
) extends NutritionService {

  import RestFatsecretApiService._

  val fatsecretConfig: FatsecretApiConfig = settingsService.fatsecretApiConfig
  val apiBaseUrl: String = fatsecretConfig.apiHost
  val authBaseUrl: String = fatsecretConfig.authHost

  override def autoCompleteFoods(query: String, maxResults: Int): Future[Either[DomainError, FoodSearchSuggestions]] =
    cache
      .getOrElseUpdate(fatsecretConfig.authTokenCacheKey, fatsecretConfig.authTokenCacheDuration) {
        getAuthToken
      }
      .flatMap { authTokenE =>
        authTokenE
          .map { authToken =>
            wsClient
              .url(s"$apiBaseUrl")
              .addQueryStringParameters(
                "method" -> "foods.autocomplete",
                "format" -> "json",
                "expression" -> query,
                "max_results" -> maxResults.toString
              )
              .addHttpHeaders("Authorization" -> s"Bearer $authToken")
              .post(EmptyBody)
              .map { response =>
                response.status match {
                  case Status.OK =>
                    Try(response.json.as[FoodSearchSuggestions]) match {
                      case Failure(e) =>
                        Left(
                          FatsecretApiError(
                            s"An error occurred while parsing JSON result:\n Result: ${response.json}\n Error: $e"
                          )
                        )
                      case Success(value) => Right(value)
                    }
                  case status => Left(FatsecretApiError(s"Unexpected status from Fatsecret API: $status"))
                }
              }
          }
          .getOrElse(Future.successful(Left(FatsecretApiError(s"Unexpected response from Fatsecret Auth API"))))
      }

  override def searchFoods(
    query: String,
    pageNumber: Int = 0,
    maxResults: Int = defaultMax,
  ): Future[Either[DomainError, FoodSearchResults]] =
    cache
      .getOrElseUpdate(fatsecretConfig.authTokenCacheKey, fatsecretConfig.authTokenCacheDuration) {
        getAuthToken
      }
      .flatMap { authTokenE =>
        authTokenE
          .map { authToken =>
            wsClient
              .url(s"$apiBaseUrl")
              .addQueryStringParameters(
                "method" -> "foods.search",
                "format" -> "json",
                "search_expression" -> query,
                "page_number" -> pageNumber.toString,
                "max_results" -> maxResults.toString
              )
              .addHttpHeaders("Authorization" -> s"Bearer $authToken")
              .post(EmptyBody)
              .map { response =>
                response.status match {
                  case Status.OK =>
                    Try(response.json.as[FoodSearchResults]) match {
                      case Failure(e) =>
                        Left(
                          FatsecretApiError(
                            s"An error occurred while parsing JSON result:\n Result: ${response.json}\n Error: $e"
                          )
                        )
                      case Success(value) => Right(value)
                    }
                  case status => Left(FatsecretApiError(s"Unexpected status from Fatsecret API: $status"))
                }
              }
          }
          .getOrElse(Future.successful(Left(FatsecretApiError(s"Unexpected response from Fatsecret Auth API"))))
      }

  override def getFoodById(
    foodId: String
  ): Future[Either[DomainError, Either[FoodGetResult, FoodGetResultSingleServing]]] =
    cache
      .getOrElseUpdate(fatsecretConfig.authTokenCacheKey, fatsecretConfig.authTokenCacheDuration) {
        getAuthToken
      }
      .flatMap { authTokenE =>
        authTokenE
          .map { authToken =>
            wsClient
              .url(s"$apiBaseUrl")
              .addQueryStringParameters("method" -> "food.get.v2", "format" -> "json", "food_id" -> foodId)
              .addHttpHeaders("Authorization" -> s"Bearer $authToken")
              .post(EmptyBody)
              .map { response =>
                response.status match {
                  case Status.OK =>
                    Try(response.json.as[FoodGetResult]) match {
                      case Failure(e) =>
                        Try(response.json.as[FoodGetResultSingleServing]) match {
                          case Failure(exception) =>
                            Left(
                              FatsecretApiError(
                                s"An error occurred while parsing JSON result:\n Result: ${response.json}\n Error: $exception"
                              )
                            )
                          case Success(value) => Right(Right(value))
                        }

                      case Success(value) => Right(Left(value))
                    }
                  case status => Left(FatsecretApiError(s"Unexpected status from Fatsecret API: $status"))
                }
              }
          }
          .getOrElse(Future.successful(Left(FatsecretApiError(s"Unexpected response from Fatsecret Auth API"))))
      }

  override def getAuthToken: Future[Either[DomainError, String]] = {
    val dataParts = Map("grant_type" -> Seq("client_credentials"), "scope" -> Seq("basic premier"))
    wsClient
      .url(authBaseUrl)
      .withAuth(fatsecretConfig.clientId, fatsecretConfig.clientSecret, WSAuthScheme.BASIC)
      .post(dataParts)
      .map { response =>
        response.status match {
          case Status.OK => Right((response.json \ "access_token").as[String])
          case status    => Left(FatsecretApiError(s"Unexpected status from Fatsecret Auth API: $status"))
        }
      }
  }
}

object RestFatsecretApiService {
  val defaultMax: Int = 50;
  val defaultAutocompleteMaxResults: Int = 5;
}
