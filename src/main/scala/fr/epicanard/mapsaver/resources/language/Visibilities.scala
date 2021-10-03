package fr.epicanard.mapsaver.resources.language

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class Visibilities(
    public: String,
    `private`: String
)

object Visibilities extends CapitalizeConfiguration
