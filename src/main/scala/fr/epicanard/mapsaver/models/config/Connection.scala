package fr.epicanard.mapsaver.models.config

case class Connection(
  host: String,
  port: Int,
  database: String,
  user: String,
  password: String,
  useSSL: Boolean
)

