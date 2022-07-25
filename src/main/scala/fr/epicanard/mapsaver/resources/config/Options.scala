package fr.epicanard.mapsaver.resources.config

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import fr.epicanard.mapsaver.models.map.Visibility
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class Options(
    pageSize: Int,
    defaultVisibility: Visibility,
    consumeEmptyMap: Boolean
)

object Options extends CapitalizeConfiguration
