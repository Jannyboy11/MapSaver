package fr.epicanard.mapsaver.resources.config

import enumeratum._
import fr.epicanard.mapsaver.circe.CirceEnumInsensitive

sealed abstract class DatabaseType(val driver: String, val jdbcHeader: String) extends EnumEntry

object DatabaseType extends Enum[DatabaseType] with CirceEnumInsensitive[DatabaseType] {
  case object MYSQL extends DatabaseType("com.mysql.jdbc.Driver", "jdbc:mysql")

  val values = findValues
}
