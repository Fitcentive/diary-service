package io.fitcentive.diary.domain.user

import play.api.libs.json.{JsString, Writes}

trait UserFitnessActivityLevel {
  def stringValue: String
}

object UserFitnessActivityLevel {
  def apply(status: String): UserFitnessActivityLevel =
    status match {
      case NotVeryActive.stringValue    => NotVeryActive
      case LightlyActive.stringValue    => LightlyActive
      case ModeratelyActive.stringValue => ModeratelyActive
      case Active.stringValue           => Active
      case VeryActive.stringValue       => VeryActive
      case _                            => throw new Exception("Unexpected user fitness activity level")
    }

  implicit lazy val writes: Writes[UserFitnessActivityLevel] = {
    {
      case NotVeryActive    => JsString(NotVeryActive.stringValue)
      case LightlyActive    => JsString(LightlyActive.stringValue)
      case ModeratelyActive => JsString(ModeratelyActive.stringValue)
      case Active           => JsString(Active.stringValue)
      case VeryActive       => JsString(VeryActive.stringValue)
    }
  }

  case object NotVeryActive extends UserFitnessActivityLevel {
    val stringValue: String = "Not very active"
  }

  case object LightlyActive extends UserFitnessActivityLevel {
    val stringValue: String = "Lightly active"
  }

  case object ModeratelyActive extends UserFitnessActivityLevel {
    val stringValue: String = "Moderately active"
  }

  case object Active extends UserFitnessActivityLevel {
    val stringValue: String = "Active"
  }

  case object VeryActive extends UserFitnessActivityLevel {
    val stringValue: String = "Very active"
  }

}
