package fr.epicanard.mapsaver.resources.language

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import fr.epicanard.mapsaver.models.map.Visibility
import io.circe.{KeyDecoder, KeyEncoder}
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class Language(
    help: Help,
    errorMessages: ErrorMessages,
    infoMessages: InfoMessages,
    mapInfo: MapInfo,
    list: List,
    pagination: Pagination,
    visibility: Map[Visibility, String]
)

object Language extends CapitalizeConfiguration {
  implicit val visibilityKeyDecoder: KeyDecoder[Visibility] = KeyDecoder.instance(key => Visibility.withNameOption(key))

  implicit val visibilityKeyEncoder: KeyEncoder[Visibility] = KeyEncoder.instance(visibility => visibility.entryName)
}
