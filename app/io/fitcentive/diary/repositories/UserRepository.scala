package io.fitcentive.diary.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.diary.domain.user.FitnessUserProfile
import io.fitcentive.diary.infrastructure.database.sql.AnormUserRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormUserRepository])
trait UserRepository {
  def deleteFitnessUserProfile(userId: UUID): Future[Unit]
  def getFitnessUserProfile(userId: UUID): Future[Option[FitnessUserProfile]]
  def upsertFitnessUserProfile(
    userId: UUID,
    fitnessUserProfileUpdate: FitnessUserProfile.Update
  ): Future[FitnessUserProfile]
}
