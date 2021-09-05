package fr.epicanard.mapsaver.resources.config

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class Config(
    language: String,
    prefix: String,
    serverName: String,
    storage: Storage,
    options: Options
)

object Config extends CapitalizeConfiguration
