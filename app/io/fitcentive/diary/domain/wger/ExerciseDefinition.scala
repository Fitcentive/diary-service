package io.fitcentive.diary.domain.wger

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Json, JsonConfiguration, OFormat}

import java.util.UUID

case class ExerciseDefinition(
  id: Long,
  name: String,
  uuid: UUID,
  description: String,
  category: ExerciseCategory,
  muscles: Seq[Muscle],
  musclesSecondary: Seq[Muscle],
  equipment: Seq[Equipment],
  language: Language,
  images: Seq[ExerciseImage]
)

object ExerciseDefinition {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[ExerciseDefinition] = Json.format[ExerciseDefinition]
}

case class ExerciseCategory(id: Long, name: String)
object ExerciseCategory {
  implicit lazy val format: OFormat[ExerciseCategory] = Json.format[ExerciseCategory]
}

case class Muscle(
  id: Long,
  name: String,
  nameEn: String,
  isFront: Boolean,
  imageUrlMain: String,
  imageUrlSecondary: String
)
object Muscle {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[Muscle] = Json.format[Muscle]
}

case class Equipment(id: Long, name: String)
object Equipment {
  implicit lazy val format: OFormat[Equipment] = Json.format[Equipment]
}

case class Language(id: Long, shortName: String, fullName: String)
object Language {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[Language] = Json.format[Language]
}

case class ExerciseImage(id: Long, uuid: UUID, exerciseBase: Long, image: String, isMain: Boolean)
object ExerciseImage {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit lazy val format: OFormat[ExerciseImage] = Json.format[ExerciseImage]
}
