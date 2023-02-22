package io.fitcentive.diary.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class RequestParametersError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("99d55d15-fe0d-4879-946b-4f071e95816c")
}
