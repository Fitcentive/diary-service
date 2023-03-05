package io.fitcentive.diary.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class FatsecretApiError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("8e7894b7-3b3e-4889-9fe7-432ea1c78804")
}
