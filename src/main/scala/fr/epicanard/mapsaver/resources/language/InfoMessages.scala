package fr.epicanard.mapsaver.resources.language

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class InfoMessages(
    newMapCreated: String,
    existingMapUpdated: String,
    newMapAssociated: String,
    mapImported: String,
    playerNoSavedMap: String,
    visibilityMapUpdated: String
)

object InfoMessages extends CapitalizeConfiguration
