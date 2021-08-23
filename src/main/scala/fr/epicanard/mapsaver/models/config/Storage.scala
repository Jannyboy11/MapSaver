package fr.epicanard.mapsaver.models.config

import io.circe.generic.JsonCodec

@JsonCodec
case class Storage(
    `type`: DatabaseType,
    tablePrefix: String,
    connection: Connection
)

object Storage {
  def toProperties(storage: Storage): Map[String, String] = Map(
    "user"              -> storage.connection.user,
    "password"          -> storage.connection.password,
    "useSSL"            -> storage.connection.useSSL.toString().toLowerCase(),
    "driver"            -> storage.`type`.driver,
    "useUnicode"        -> "true",
    "characterEncoding" -> "UTF-8",
    "autoReconnect"     -> "true"
  )

  def buildUrl(storage: Storage): String =
    s"${storage.`type`.jdbcHeader}://${storage.connection.host}:${storage.connection.port}/${storage.connection.database}"
}
