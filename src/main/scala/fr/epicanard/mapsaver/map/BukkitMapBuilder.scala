package fr.epicanard.mapsaver.map

import fr.epicanard.mapsaver.errors.TechnicalError.MissingMapRenderer
import fr.epicanard.mapsaver.errors.{Error, TechnicalError}
import fr.epicanard.mapsaver.reflection.Reflection.{getFieldByName, getFieldByType}
import org.bukkit.Bukkit
import org.bukkit.map.{MapRenderer, MapView}

import scala.annotation.nowarn
import scala.jdk.CollectionConverters._

object BukkitMapBuilder {

  object MapViewBuilder {
    def newLockedWithColors(bytes: Array[Byte]): Either[Error, MapView] =
      updateMapColors(newLocked, bytes)

    @nowarn
    def fromId(id: Int): MapView = Bukkit.getMap(id)

    def updateMapColors(mapView: MapView, bytes: Array[Byte]): Either[Error, MapView] =
      getRenderer(mapView).map { renderer =>
        getColorsMap(renderer).map { colorsMap =>
          Array.copy(bytes, 0, colorsMap.bytes, 0, bytes.length)
          colorsMap.maybeVanillaBuffer.map(buffer => Array.copy(bytes, 0, buffer, 0, bytes.length))
        }
        mapView
      }

    private def newLocked: MapView = {
      val mapView = Bukkit.createMap(Bukkit.getWorlds.get(0))
      mapView.setLocked(true)
      mapView
    }

    def getRenderer(mapView: MapView): Either[Error, MapRenderer] = {
      val renderers = mapView.getRenderers.asScala.toList
      renderers
        .find(_.getClass.getCanonicalName == "CraftMapRenderer")
        .orElse(renderers.headOption)
        .toRight[Error](MissingMapRenderer(mapView.getId))
    }
  }

  def getColorsMap(mapRenderer: MapRenderer): Either[TechnicalError, ColorsMap] =
    for {
      worldMap <- getFieldByName[Any](mapRenderer, "worldMap")
      vanillaBuffer <- Right(
        getFieldByName[Any](worldMap, "vanillaRender").flatMap(getFieldByName[Array[Byte]](_, "buffer")).toOption
      )
      bytes <- getFieldByType[Array[Byte]](worldMap, "byte[]")
    } yield ColorsMap(vanillaBuffer, bytes)
}
