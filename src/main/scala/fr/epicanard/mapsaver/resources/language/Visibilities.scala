package fr.epicanard.mapsaver.resources.language

import fr.epicanard.mapsaver.circe.CapitalizeConfiguration
import fr.epicanard.mapsaver.models.map.Visibility
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec
case class Visibilities(
    public: String,
    `private`: String
)

object Visibilities extends CapitalizeConfiguration {
  def fromVisibility(visibility: Visibility, visibilities: Visibilities): String = visibility match {
    case Visibility.Public  => visibilities.public
    case Visibility.Private => visibilities.`private`
  }
}
