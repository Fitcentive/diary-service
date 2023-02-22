//package io.fitcentive.diary.infrastructure.database.sql
//
//import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
//import io.fitcentive.sdk.infrastructure.database.DatabaseClient
//import io.fitcentive.sdk.utils.AnormOps
//import io.fitcentive.diary.repositories.ExerciseDiaryRepository
//import play.api.db.Database
//
//import javax.inject.{Inject, Singleton}
//import scala.concurrent.Future
//
//@Singleton
//class AnormExerciseDiaryRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
//  extends ExerciseDiaryRepository
//  with DatabaseClient {
//
//  import AnormExerciseDiaryRepository._
//
//}
//
//object AnormExerciseDiaryRepository extends AnormOps {}
