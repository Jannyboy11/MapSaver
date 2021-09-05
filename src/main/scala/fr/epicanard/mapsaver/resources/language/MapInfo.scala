package fr.epicanard.mapsaver.resources.language

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class MapInfo(
    name: String,
    owner: String,
    visibility: String,
    originalMap: String,
    copyMaps: String,
    actions: String
)

object MapInfo extends CapitalizeConfiguration
