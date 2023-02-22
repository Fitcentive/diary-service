package io.fitcentive.diary.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class WgerApiError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("7e45e3a4-474f-4af4-92f0-1135305266c1")
}
