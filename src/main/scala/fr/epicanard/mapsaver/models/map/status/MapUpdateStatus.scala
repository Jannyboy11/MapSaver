package fr.epicanard.mapsaver.models.map.status

import fr.epicanard.mapsaver.resources.language.InfoMessages

sealed trait MapUpdateStatus

object MapUpdateStatus {
  case object ExistingMapUpdated extends MapUpdateStatus

  def getMessage(mapUpdateStatus: MapUpdateStatus, infoMessages: InfoMessages): String = mapUpdateStatus match {
    case ExistingMapUpdated => infoMessages.existingMapUpdated
  }
}
