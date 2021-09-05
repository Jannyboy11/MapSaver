package fr.epicanard.mapsaver.resources.language

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class List(
    listMaps: String,
    infoHover: String,
    importHover: String
)

object List extends CapitalizeConfiguration
