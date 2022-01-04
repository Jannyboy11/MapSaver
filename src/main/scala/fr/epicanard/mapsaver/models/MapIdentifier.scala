package fr.epicanard.mapsaver.models

import java.util.UUID

sealed trait MapIdentifier

object MapIdentifier {
  case class MapId(mapId: Int, server: String)     extends MapIdentifier
  case class MapName(mapName: String, owner: UUID) extends MapIdentifier
}
