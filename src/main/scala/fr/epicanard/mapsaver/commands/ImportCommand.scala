package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.commands.CommandContext.getPlayer
import fr.epicanard.mapsaver.commands.ImportCommand.{buildItem, parseArguments}
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.MissingMapName
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.message.Component.toColor
import fr.epicanard.mapsaver.models.Player
import fr.epicanard.mapsaver.models.map.Visibility
import fr.epicanard.mapsaver.resources.language.{Help, Language}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.{Material, OfflinePlayer}

import java.util
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ImportCommand(mapRepository: MapRepository) extends BaseCommand(Some(Permission.ImportMap)) {
  def helpMessage(help: Help): String = help.`import`

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    (for {
      player <- EitherT.fromEither[Future](getPlayer(commandContext))
      args   <- EitherT.fromEither[Future](parseArguments(commandContext, player))
      restrictVisibility = Option.unless(Permission.AdminImportMap.isSetOn(player))(Visibility.Public)
      mapView <- EitherT(
        mapRepository
          .findMapView(args.owner.getUniqueId, args.mapName, commandContext.config.serverName, restrictVisibility)
      )
      item      = buildItem(mapView, args.mapName, messenger.language, args.owner.getName)
      _         = player.getInventory.addItem(item)
      statusMsg = msg"${messenger.language.infoMessages.mapImported}"
    } yield statusMsg).value

  def onTabComplete(commandContext: CommandContext): List[String] = Nil
}

object ImportCommand {
  private case class ImportArgs(mapName: String, owner: OfflinePlayer)

  private def parseArguments(commandContext: CommandContext, senderPlayer: OfflinePlayer): Either[Error, ImportArgs] =
    commandContext.args match {
      case name :: Nil         => Right(ImportArgs(name, senderPlayer))
      case name :: player :: _ => Right(ImportArgs(name, Player.getOfflinePlayer(player)))
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
}
