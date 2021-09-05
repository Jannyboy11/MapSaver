package fr.epicanard.mapsaver.models.map

import enumeratum.EnumEntry
import enumeratum.Enum
import enumeratum.EnumEntry.UpperSnakecase

sealed trait Visibility extends EnumEntry with UpperSnakecase

object Visibility extends Enum[Visibility] {
  case object Public  extends Visibility
  case object Private extends Visibility

  val values = findValues
}
