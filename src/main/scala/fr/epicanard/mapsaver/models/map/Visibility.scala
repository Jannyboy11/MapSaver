package fr.epicanard.mapsaver.models.map

import enumeratum.EnumEntry
import enumeratum.Enum

sealed trait Visibility extends EnumEntry

object Visibility extends Enum[Visibility] {
  case object PUBLIC  extends Visibility
  case object PRIVATE extends Visibility

  val values = findValues
}
