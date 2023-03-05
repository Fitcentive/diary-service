package io.fitcentive.diary.infrastructure.settings

import anorm.SqlParser.int
import io.fitcentive.diary.services.HealthService
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import play.api.db.Database

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class AppHealthService @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends HealthService
  with DatabaseClient {

  import AppHealthService._

  override def isSqlDatabaseAvailable: Future[Boolean] =
    Future {
      getRecordOpt(SQL_SELECT_1)(int(1)).isDefined
    }(dbec)

}

object AppHealthService {
  val SQL_SELECT_1: String = "SELECT 1"

}
