package io.fitcentive.diary.infrastructure.utils

import io.fitcentive.sdk.error.{DomainError, EntityConflictError, EntityNotAccessible, EntityNotFoundError}
import io.fitcentive.sdk.logging.AppLogger
import io.fitcentive.sdk.utils.DomainErrorHandler
import io.fitcentive.diary.domain.errors._
import play.api.mvc.Result
import play.api.mvc.Results._

trait ServerErrorHandler extends DomainErrorHandler with AppLogger {

  override def resultErrorAsyncHandler: PartialFunction[Throwable, Result] = {
    case e: Exception =>
      logError(s"${e.getMessage}", e)
      InternalServerError(e.getMessage)
  }

  override def domainErrorHandler: PartialFunction[DomainError, Result] = {
    case TokenVerificationError(reason) => Unauthorized(reason)
    case RequestParametersError(reason) => BadRequest(reason)
    case EntityNotFoundError(reason)    => NotFound(reason)
    case EntityConflictError(reason)    => Conflict(reason)
    case EntityNotAccessible(reason)    => Forbidden(reason)
    case WgerApiError(reason)           => BadRequest(reason)
    case FatsecretApiError(reason)      => BadRequest(reason)
    case _                              => InternalServerError("Unexpected error occurred ")
  }

}
