package fr.epicanard.mapsaver.resources.config

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class Connection(
    host: String,
    port: Int,
    database: String,
    user: String,
    password: String,
    useSSL: Boolean
)

object Connection extends CapitalizeConfiguration
