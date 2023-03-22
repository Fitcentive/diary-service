package io.fitcentive.diary.domain.food

import play.api.libs.json.{JsString, Writes}

trait MealEntry {
  def stringValue: String
}

object MealEntry {
  def apply(status: String): MealEntry =
    status match {
      case Breakfast.stringValue => Breakfast
      case Lunch.stringValue     => Lunch
      case Dinner.stringValue    => Dinner
      case Snack.stringValue     => Snack
      case _                     => throw new Exception("Unexpected meal entry")
    }

  implicit lazy val writes: Writes[MealEntry] = {
    {
      case Breakfast => JsString(Breakfast.stringValue)
      case Lunch     => JsString(Lunch.stringValue)
      case Dinner    => JsString(Dinner.stringValue)
      case Snack     => JsString(Snack.stringValue)
    }
  }

  case object Breakfast extends MealEntry {
    val stringValue: String = "Breakfast"
  }

  case object Lunch extends MealEntry {
    val stringValue: String = "Lunch"
  }

  case object Dinner extends MealEntry {
    val stringValue: String = "Dinner"
  }

  case object Snack extends MealEntry {
    val stringValue: String = "Snack"
  }
}
