package fr.epicanard.mapsaver.map

import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.MapInHandNeeded
import fr.epicanard.mapsaver.errors.TechnicalError.{InvalidMapMeta, InvalidMapView}
import fr.epicanard.mapsaver.map.BukkitMapBuilder.{getColorsMap, MapViewBuilder}
import fr.epicanard.mapsaver.models.map.MapItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.{ItemMeta, MapMeta}
import org.bukkit.map.MapView

object MapExtractor {

  def extractMapView(player: Player): Either[Error, MapView] = {
    val stack = player.getInventory.getItemInMainHand
    for {
      _       <- Either.cond(stack.getType == Material.FILLED_MAP, (), MapInHandNeeded)
      mapMeta <- extractMapMeta(stack.getItemMeta).toRight[Error](InvalidMapMeta(player))
      mapView <- Option(mapMeta.getMapView).toRight[Error](InvalidMapView(player))
    } yield mapView
  }

  def extractFromPlayer(player: Player): Either[Error, MapItem] =
    for {
      mapView     <- extractMapView(player)
      mapRenderer <- MapViewBuilder.getRenderer(mapView)
      colorsMap   <- getColorsMap(mapRenderer)
      byteMap = new Array[Byte](16384)
      _       = Array.copy(colorsMap.bytes, 0, byteMap, 0, 16384)
    } yield MapItem(
      id = mapView.getId,
      bytes = byteMap
    )

  private def extractMapMeta(itemMeta: ItemMeta): Option[MapMeta] = itemMeta match {
    case mapMeta: MapMeta => Some(mapMeta)
    case _                => None
  }
}
