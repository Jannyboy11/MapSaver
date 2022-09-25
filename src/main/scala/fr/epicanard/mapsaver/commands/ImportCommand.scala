package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import cats.implicits._
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.commands.CommandContext.getPlayer
import fr.epicanard.mapsaver.commands.ImportCommand.{buildItem, parseArguments}
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.{InventoryFull, MissingEmptyMap, MissingMapName}
import fr.epicanard.mapsaver.map.BukkitMapBuilder.MapViewBuilder
import fr.epicanard.mapsaver.message.Component.toColor
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.models.map.Visibility
import fr.epicanard.mapsaver.models.{Complete, Player}
import fr.epicanard.mapsaver.resources.language.{Help, Language}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.{entity, Material, OfflinePlayer}

import java.util
import scala.concurrent.{ExecutionContext, Future}

case class ImportCommand(mapRepository: MapRepository)(implicit ec: ExecutionContext)
    extends BaseCommand(Some(Permission.ImportMap)) {
  def helpMessage(help: Help): String = help.`import`

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    (for {
      player <- EitherT.fromEither[Future](getPlayer(commandContext))
      args   <- EitherT.fromEither[Future](parseArguments(commandContext, player))
      restrictVisibility = Visibility.getRestrictVisibility(commandContext, args.owner, Permission.AdminImportMap)
      mapViewWithData <- EitherT(
        mapRepository
          .findMapViewWithData(args.owner.getUniqueId, args.mapName, commandContext.server, restrictVisibility)
      )
      item = buildItem(mapViewWithData.mapView, args.mapName, messenger.language, args.owner.getName)
      _ <- EitherT.fromEither[Future](
        MapViewBuilder.updateMapColors(mapViewWithData.mapView, mapViewWithData.dataMap.bytes)
      )
      maybeMapToConsume <- EitherT.fromEither[Future](
        ImportCommand.getItemToConsume(commandContext.config.options.consumeEmptyMap, player)
      )
      _ <- EitherT.cond[Future](player.getInventory.addItem(item).size <= 0, (), InventoryFull: Error)
    } yield {
      maybeMapToConsume.foreach { case (it, index) =>
        it.setAmount(it.getAmount() - 1)
        player.getInventory.setItem(index, it)
      }

      msg"${messenger.language.infoMessages.mapImported}"
    }).value

  def onTabComplete(commandContext: CommandContext): Future[Either[Error, Complete]] =
    BaseCommand.mapTabComplete(mapRepository, commandContext)
}

object ImportCommand {
  private case class ImportArgs(mapName: String, owner: OfflinePlayer)

  private def parseArguments(commandContext: CommandContext, senderPlayer: OfflinePlayer): Either[Error, ImportArgs] =
    commandContext.args match {
      case name :: Nil         => Right(ImportArgs(name, senderPlayer))
      case player :: name :: _ => Right(ImportArgs(name, Player.getOfflinePlayer(player)))
      case Nil                 => Left(MissingMapName)
    }

  private def buildItem(mapView: MapView, mapName: String, language: Language, playerName: String): ItemStack = {
    val mapItem = new ItemStack(Material.FILLED_MAP, 1)
    val meta    = mapItem.getItemMeta.asInstanceOf[MapMeta]

    val lore = Option(meta.getLore).getOrElse(new util.ArrayList[String]())
    lore.add(toColor(s"&6${language.mapInfo.name} : &f$mapName"))
    lore.add(toColor(s"&6${language.mapInfo.owner} : &f$playerName"))
    meta.setLore(lore)

    meta.setMapView(mapView)
    mapItem.setItemMeta(meta)
    mapItem
  }

  def getItemToConsume(consumeEmptyMap: Boolean, player: entity.Player): Either[Error, Option[(ItemStack, Int)]] =
    if (consumeEmptyMap) {
      player.getInventory
        .getStorageContents()
        .zipWithIndex
        .find { case (it, _) => it != null && it.getType().equals(Material.MAP) }
        .toRight(MissingEmptyMap: Error)
        .map(Some(_))
    } else {
      Right(None)
    }
}
