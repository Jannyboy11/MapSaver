package fr.epicanard.mapsaver.models.config

import io.circe.Decoder
import scala.util.Try
import enumeratum._
import fr.epicanard.mapsaver.models.circe.CirceEnumInsensitive

sealed abstract class DatabaseType(val driver: String) extends EnumEntry

object DatabaseType extends Enum[DatabaseType] with CirceEnumInsensitive[DatabaseType] {
  case object MYSQL extends DatabaseType("com.mysql.cj.jdbc.Driver")

  val values = findValues
}
