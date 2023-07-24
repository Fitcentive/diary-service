package io.fitcentive.diary.domain.user

import play.api.libs.json.{JsString, Writes}

trait UserFitnessGoal {
  def stringValue: String
}

object UserFitnessGoal {
  def apply(status: String): UserFitnessGoal =
    status match {
      case LoseHalfPoundPerWeek.stringValue        => LoseHalfPoundPerWeek
      case LoseOnePoundPerWeek.stringValue         => LoseOnePoundPerWeek
      case LoseOneAndHalfPoundPerWeek.stringValue  => LoseOneAndHalfPoundPerWeek
      case LoseTwoPoundsPerWeek.stringValue        => LoseTwoPoundsPerWeek
      case MaintainWeight.stringValue              => MaintainWeight
      case GainHalfPoundPerWeek.stringValue        => GainHalfPoundPerWeek
      case GainOnePoundPerWeek.stringValue         => GainOnePoundPerWeek
      case GainOneAndHalfPoundsPerWeek.stringValue => GainOneAndHalfPoundsPerWeek
      case GainTwoPoundsPerWeek.stringValue        => GainTwoPoundsPerWeek
      case _                                       => throw new Exception("Unexpected user fitness goals")
    }

  implicit lazy val writes: Writes[UserFitnessGoal] = {
    {
      case LoseHalfPoundPerWeek        => JsString(LoseHalfPoundPerWeek.stringValue)
      case LoseOnePoundPerWeek         => JsString(LoseOnePoundPerWeek.stringValue)
      case LoseOneAndHalfPoundPerWeek  => JsString(LoseOneAndHalfPoundPerWeek.stringValue)
      case LoseTwoPoundsPerWeek        => JsString(LoseTwoPoundsPerWeek.stringValue)
      case MaintainWeight              => JsString(MaintainWeight.stringValue)
      case GainHalfPoundPerWeek        => JsString(GainHalfPoundPerWeek.stringValue)
      case GainOnePoundPerWeek         => JsString(GainOnePoundPerWeek.stringValue)
      case GainOneAndHalfPoundsPerWeek => JsString(GainOneAndHalfPoundsPerWeek.stringValue)
      case GainTwoPoundsPerWeek        => JsString(GainTwoPoundsPerWeek.stringValue)
    }
  }

  case object LoseHalfPoundPerWeek extends UserFitnessGoal {
    val stringValue: String = "Lose 0.5 lbs per week"
  }

  case object LoseOnePoundPerWeek extends UserFitnessGoal {
    val stringValue: String = "Lose 1 lbs per week"
  }

  case object LoseOneAndHalfPoundPerWeek extends UserFitnessGoal {
    val stringValue: String = "Lose 1.5 lbs per week"
  }

  case object LoseTwoPoundsPerWeek extends UserFitnessGoal {
    val stringValue: String = "Lose 2 lbs per week"
  }

  case object MaintainWeight extends UserFitnessGoal {
    val stringValue: String = "Maintain weight"
  }

  case object GainHalfPoundPerWeek extends UserFitnessGoal {
    val stringValue: String = "Gain 0.5 lbs per week"
  }

  case object GainOnePoundPerWeek extends UserFitnessGoal {
    val stringValue: String = "Gain 1 lbs per week"
  }

  case object GainOneAndHalfPoundsPerWeek extends UserFitnessGoal {
    val stringValue: String = "Gain 1.5 lbs per week"
  }

  case object GainTwoPoundsPerWeek extends UserFitnessGoal {
    val stringValue: String = "Gain 2 lbs per week"
  }

}
