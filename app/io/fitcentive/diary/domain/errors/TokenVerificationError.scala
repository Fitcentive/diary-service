package io.fitcentive.diary.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class TokenVerificationError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("ad3bfd71-eabb-4b88-ac49-1dc3d41f14f1")
}
