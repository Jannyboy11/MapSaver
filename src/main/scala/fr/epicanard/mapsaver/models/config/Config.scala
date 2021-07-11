package fr.epicanard.mapsaver.models.config

import io.circe.generic.JsonCodec

@JsonCodec
case class Config(
  language: String,
  prefix: String,
  serverName: String,
  storage: Storage,
  options: Options
)

