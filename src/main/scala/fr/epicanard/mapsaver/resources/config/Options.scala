package fr.epicanard.mapsaver.resources.config

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class Options(
    pageSize: Int,
    defaultVisibility: String,
    consumeEmptyMap: Boolean
)

object Options extends CapitalizeConfiguration
