package fr.epicanard.mapsaver.resources.language

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class ErrorMessages(
    permissionDenied: String,
    playerOnlyCommand: String,
    mapInHandNeeded: String,
    missingMapName: String,
    missingMapOrNotPublic: String,
    missingDataMap: String,
    missingEmptyMap: String,
    notTheOwner: String,
    notTheOriginal: String,
    alreadySaved: String,
    wrongVisibility: String,
    invalidPageNumber: String,
    unexpectedError: String,
    lockedMapDenied: String,
    inventoryFull: String
)

object ErrorMessages extends CapitalizeConfiguration
