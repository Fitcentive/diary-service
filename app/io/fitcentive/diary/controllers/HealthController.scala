package io.fitcentive.diary.controllers

//import io.fitcentive.diary.services.HealthService
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class HealthController @Inject() (cc: ControllerComponents)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

//  def readinessProbe: Action[AnyContent] =
//    Action.async {
//      healthService.isSqlDatabaseAvailable.map {
//        case true => Ok("Server is alive!")
//        case _    => NotFound
//      }
//    }

  def livenessProbe: Action[AnyContent] = Action { Ok("Server is alive!") }

  def readinessProbe: Action[AnyContent] = Action { Ok("Server is alive!") }

}
