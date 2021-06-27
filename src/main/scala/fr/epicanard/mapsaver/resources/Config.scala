package fr.epicanard.mapsaver.resources

import io.circe._, io.circe.generic.semiauto._

case class Config(
  language: String,
  prefix: String,
  serverName: String,
  storage: Storage,
  options: Options,
  privacy: Privacy
)

case class Storage(
  `type`: String,
  tablePrefix: String,
  connection: Connection
)

case class Connection(
  host: String,
  port: Int,
  database: String,
  user: String,
  password: String,
  useSSL: Boolean
)

case class Options(
  pageSize: Int
)

case class Privacy(
  defaultVisibility: String
)

object Config {
    given configDecoder: Decoder[Config] = deriveDecoder
}
