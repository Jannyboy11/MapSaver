package fr.epicanard.mapsaver.models.config

import io.circe.Decoder
import scala.util.Try

enum DatabaseType(val driver: String):
  case MYSQL extends DatabaseType("com.mysql.cj.jdbc.Driver")

object DatabaseType:
  given databaseTypeDecoder: Decoder[DatabaseType] = Decoder.decodeString.emapTry { str =>
    Try(DatabaseType.valueOf(str.toUpperCase))
  }

