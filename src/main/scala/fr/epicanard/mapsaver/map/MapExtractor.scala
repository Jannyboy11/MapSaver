package fr.epicanard.mapsaver.map

import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.MapInHandNeeded
import fr.epicanard.mapsaver.errors.TechnicalError.{InvalidMapMeta, MissingMapRenderer}
import fr.epicanard.mapsaver.models.map.MapItem
import fr.epicanard.mapsaver.reflection.Reflection._
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.{ItemMeta, MapMeta}

import scala.jdk.CollectionConverters._

object MapExtractor {

  def extractFromPlayer(player: Player): Either[Error, MapItem] = {
    val stack = player.getInventory.getItemInMainHand
    for {
      _       <- Either.cond(stack.getType == Material.FILLED_MAP, (), MapInHandNeeded)
      mapMeta <- extractMapMeta(stack.getItemMeta).toRight[Error](InvalidMapMeta(player))
      mapView = mapMeta.getMapView
      mapRenderer <- mapView.getRenderers.asScala.toList.headOption
        .toRight[Error](MissingMapRenderer(player, mapView.getId))
      worldMap  <- getFieldByName[Any](mapRenderer, "worldMap")
      colorsMap <- getFieldByType[Array[Byte]](worldMap, "byte[]")
      byteMap = new Array[Byte](16384)
      _       = Array.copy(colorsMap, 0, byteMap, 0, 16384)
    } yield MapItem(
      id = mapView.getId,
      bytes = byteMap
    )
  }

  private def extractMapMeta(itemMeta: ItemMeta): Option[MapMeta] = itemMeta match {
    case mapMeta: MapMeta => Some(mapMeta)
    case _                => None
  }
}
