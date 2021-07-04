package fr.epicanard.mapsaver.models.config

case class Storage(
  `type`: DatabaseType,
  tablePrefix: String,
  connection: Connection
)

