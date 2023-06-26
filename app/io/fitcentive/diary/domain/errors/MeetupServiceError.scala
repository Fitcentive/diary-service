package io.fitcentive.diary.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class MeetupServiceError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("a903dbf1-4893-4fac-bac3-a154bfd21010")
}
