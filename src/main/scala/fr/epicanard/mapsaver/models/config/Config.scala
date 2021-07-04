package fr.epicanard.mapsaver.models.config

import io.circe._, io.circe.generic.semiauto._

case class Config(
  language: String,
  prefix: String,
  serverName: String,
  storage: Storage,
  options: Options
)

object Config:
  given configDecoder: Decoder[Config] = deriveDecoder

