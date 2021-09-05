package fr.epicanard.mapsaver.resources.config

import io.circe.generic.JsonCodec

@JsonCodec
case class Config(
    language: String,
    prefix: String,
    serverName: String,
    storage: Storage,
    options: Options
)
