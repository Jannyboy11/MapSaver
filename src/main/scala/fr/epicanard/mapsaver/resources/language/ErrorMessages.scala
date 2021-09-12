package fr.epicanard.mapsaver.resources.language

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class ErrorMessages(
    permissionNotAllowed: String,
    playerOnlyCommand: String,
    mapInHandNeeded: String,
    missingMapName: String,
    missingMapOrNotPublic: String,
    missingDataMap: String,
    notTheOwner: String,
    notTheOriginal: String,
    alreadySaved: String,
    wrongVisibility: String,
    pageNumberNotValid: String,
    unexpectedError: String
)

object ErrorMessages extends CapitalizeConfiguration
