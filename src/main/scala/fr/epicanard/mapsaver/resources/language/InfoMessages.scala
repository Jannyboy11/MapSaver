package fr.epicanard.mapsaver.resources.language

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class InfoMessages(
    creatingNewMap: String,
    updatingExistingMap: String,
    associationNewMap: String,
    playerNoSavedMap: String
)

object InfoMessages extends CapitalizeConfiguration
