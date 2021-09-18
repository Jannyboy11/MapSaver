package fr.epicanard.mapsaver.models.map

import fr.epicanard.mapsaver.resources.language.InfoMessages

sealed trait MapCreationStatus

object MapCreationStatus {
  case object Created    extends MapCreationStatus
  case object Associated extends MapCreationStatus

  def getMessage(mapCreationStatus: MapCreationStatus, infoMessages: InfoMessages): String = mapCreationStatus match {
    case Created    => infoMessages.newMapCreated
    case Associated => infoMessages.newMapAssociated
  }
}
