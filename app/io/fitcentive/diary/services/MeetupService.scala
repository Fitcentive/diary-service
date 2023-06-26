package io.fitcentive.diary.services

import com.google.inject.ImplementedBy
import io.fitcentive.diary.infrastructure.rest.RestMeetupService
import io.fitcentive.sdk.error.DomainError

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestMeetupService])
trait MeetupService {
  def deleteCardioEntryAssociatedToMeetup(diaryEntryId: UUID): Future[Either[DomainError, Unit]]
  def deleteStrengthEntryAssociatedToMeetup(diaryEntryId: UUID): Future[Either[DomainError, Unit]]
  def deleteFoodEntryAssociatedToMeetup(diaryEntryId: UUID): Future[Either[DomainError, Unit]]
}
