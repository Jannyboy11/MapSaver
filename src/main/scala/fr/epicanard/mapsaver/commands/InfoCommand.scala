package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.commands.CommandContext.getPlayer
import fr.epicanard.mapsaver.commands.InfoCommand.{buildMessage, getPlayerServerMaps}
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.map.MapExtractor
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Component, Message, Messenger}
import fr.epicanard.mapsaver.models.map.{PlayerServerMaps, Visibility}
import fr.epicanard.mapsaver.models.{Complete, Player, RestrictVisibility}
import fr.epicanard.mapsaver.resources.language.{Help, Language, Visibilities}
import net.md_5.bungee.api.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender

import scala.concurrent.{ExecutionContext, Future}

case class InfoCommand(mapRepository: MapRepository)(implicit ec: ExecutionContext)
    extends BaseCommand(Some(Permission.ListMap)) {

  def helpMessage(help: Help): String = help.info

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    (for {
      playerServerMaps <- EitherT(getPlayerServerMaps(mapRepository, commandContext))
      message = buildMessage(messenger.language, playerServerMaps, commandContext.sender)
    } yield message).value

  def onTabComplete(commandContext: CommandContext): Future[Either[Error, Complete]] =
    BaseCommand.mapTabComplete(mapRepository, commandContext)
}

object InfoCommand {
  private case class MapsWithOwner(playerServerMaps: PlayerServerMaps, owner: OfflinePlayer)

  private def getPlayerServerMaps(
      mapRepository: MapRepository,
      commandContext: CommandContext
  )(implicit ec: ExecutionContext): Future[Either[Error, MapsWithOwner]] =
    commandContext.args match {
      case Nil                 => getInfoMapInHand(mapRepository, commandContext)
      case name :: Nil         => getInfoOfSender(mapRepository, commandContext, name)
      case player :: name :: _ => getInfoOfPlayer(mapRepository, commandContext, name, player)
    }

  private def getInfoMapInHand(
      mapRepository: MapRepository,
      commandContext: CommandContext
  )(implicit ec: ExecutionContext): Future[Either[Error, MapsWithOwner]] =
    (for {
      player <- EitherT.fromEither[Future](getPlayer(commandContext))
      restrictVisibility = RestrictVisibility.unless(Permission.AdminInfoMap, player)(RestrictVisibility.OwnerOrPublic)
      mapView <- EitherT.fromEither[Future](MapExtractor.extractMapView(player))
      playerServerMaps <- EitherT(
        mapRepository
          .getMapInfo(player.getUniqueId, restrictVisibility, mapView.getId, commandContext.server)
      )
      owner = Player.getOfflinePlayer(playerServerMaps.playerMap.playerUuid)
    } yield MapsWithOwner(playerServerMaps, owner)).value

  private def getInfoOfSender(
      mapRepository: MapRepository,
      commandContext: CommandContext,
      name: String
  )(implicit ec: ExecutionContext): Future[Either[Error, MapsWithOwner]] =
    (for {
      player           <- EitherT.fromEither[Future](getPlayer(commandContext))
      playerServerMaps <- EitherT(mapRepository.getMapInfo(player.getUniqueId, None, name))
    } yield MapsWithOwner(playerServerMaps, player)).value

  private def getInfoOfPlayer(
      mapRepository: MapRepository,
      commandContext: CommandContext,
      name: String,
      playerName: String
  )(implicit ec: ExecutionContext): Future[Either[Error, MapsWithOwner]] = {
    val owner              = Player.getOfflinePlayer(playerName)
    val restrictVisibility = Visibility.getRestrictVisibility(commandContext, owner, Permission.AdminInfoMap)
    EitherT(mapRepository.getMapInfo(owner.getUniqueId, restrictVisibility, name))
      .map(maps => MapsWithOwner(maps, owner))
      .value
  }

  private def buildMessage(
      language: Language,
      mapsWithOwner: MapsWithOwner,
      sender: CommandSender
  ): Message = {
    val mapInfo          = language.mapInfo
    val playerServerMaps = mapsWithOwner.playerServerMaps
    val visibility       = Visibilities.fromVisibility(playerServerMaps.playerMap.visibility, language.visibilities)
    msg"""&7-------------------
       |&6${mapInfo.name} : &f${playerServerMaps.playerMap.name}
       |&6${mapInfo.owner} : &f${mapsWithOwner.owner.getName}
       |&6${mapInfo.visibility} : &f$visibility
       |&6${mapInfo.originalMap} : &f${playerServerMaps.originalMap.originalId.get} - ${playerServerMaps.originalMap.server}#${playerServerMaps.originalMap.world} - [${playerServerMaps.originalMap.x},${playerServerMaps.originalMap.z}]
       |&6${mapInfo.copyMaps} : &f""" ++
      Message(playerServerMaps.serverMaps.foldLeft(List[Component]()) { case (comps, serverMap) =>
        comps :+ Component(s" • ${serverMap.lockedId} - ${serverMap.server}")
      }) +?
      Option.when(Permission.ImportMap.isSetOn(sender, defaultSender = false))(
        Component(s"&6${mapInfo.actions} : &f") + Component.link(
          "import",
          language.list.importHover,
          ChatColor.DARK_GREEN,
          s"""/mapsaver import ${mapsWithOwner.owner.getName} "${playerServerMaps.playerMap.name}""""
        )
      ) +?
      Option.when(playerServerMaps.playerMap.locked)(Component(s"&c${mapInfo.locked}")) ++
      msg"""&7-------------------"""
  }
}
