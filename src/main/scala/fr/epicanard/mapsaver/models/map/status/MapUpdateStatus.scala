package fr.epicanard.mapsaver.models.map.status

import fr.epicanard.mapsaver.resources.language.InfoMessages

sealed trait MapUpdateStatus

object MapUpdateStatus {
  case class ExistingMapUpdated(dataId: Int) extends MapUpdateStatus

  def getMessage(mapUpdateStatus: MapUpdateStatus, infoMessages: InfoMessages): String = mapUpdateStatus match {
    case _: ExistingMapUpdated => infoMessages.existingMapUpdated
  }

  def dataId(mapUpdateStatus: MapUpdateStatus): Int = mapUpdateStatus match {
    case ExistingMapUpdated(dataId) => dataId
  }
}
