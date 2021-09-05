package fr.epicanard.mapsaver.resources.language

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class Pagination(
    previousPageHover: String,
    nextPageHover: String
)

object Pagination extends CapitalizeConfiguration
