package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.commands.CommandContext.{getPlayer, getPlayerOpt}
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.{Error, MapSaverError}
import fr.epicanard.mapsaver.map.MapExtractor
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.models.map.{PlayerMap, PlayerServerMaps, Visibility}
import fr.epicanard.mapsaver.models.{Player, RestrictVisibility}
import fr.epicanard.mapsaver.resources.language.Help
import org.bukkit.OfflinePlayer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class DeleteCommand(mapRepository: MapRepository) extends BaseCommand(Some(Permission.DeleteMap)) {

  def helpMessage(help: Help): String = help.delete

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    (for {
      playerMap <- EitherT(DeleteCommand.getPlayerServerMaps(mapRepository, commandContext))
      _         <- EitherT(mapRepository.deletePlayerMap(playerMap.playerUuid, playerMap.name))
    } yield msg"""${messenger.language.infoMessages.mapDeleted}""").value

  def onTabComplete(commandContext: CommandContext): List[String] = Nil
}

object DeleteCommand {
  private def getPlayerServerMaps(
      mapRepository: MapRepository,
      commandContext: CommandContext
  ): Future[Either[Error, PlayerMap]] =
    commandContext.args match {
      case Nil                 => getInfoMapInHand(mapRepository, commandContext)
      case name :: Nil         => getInfoOfSender(mapRepository, commandContext, name)
      case name :: player :: _ => getInfoOfPlayer(mapRepository, commandContext, name, player)
    }

  private def getInfoMapInHand(
      mapRepository: MapRepository,
      commandContext: CommandContext
  ): Future[Either[Error, PlayerMap]] =
    (for {
      player <- EitherT.fromEither[Future](getPlayer(commandContext))
      restrictVisibility = RestrictVisibility.unless(Permission.AdminDeleteMap, player)(RestrictVisibility.OwnerOnly)
      mapView <- EitherT.fromEither[Future](MapExtractor.extractMapView(player))
      playerServerMaps <- EitherT(
        mapRepository
          .getMapInfo(player.getUniqueId, restrictVisibility, mapView.getId, commandContext.server)
      )
    } yield playerServerMaps.playerMap).value

  private def getInfoOfSender(
      mapRepository: MapRepository,
      commandContext: CommandContext,
      name: String
  ): Future[Either[Error, PlayerMap]] =
    (for {
      player           <- EitherT.fromEither[Future](getPlayer(commandContext))
      playerServerMaps <- EitherT(mapRepository.getMapInfo(player.getUniqueId, None, name))
    } yield playerServerMaps.playerMap).value

  private def getInfoOfPlayer(
      mapRepository: MapRepository,
      commandContext: CommandContext,
      name: String,
      playerName: String
  ): Future[Either[Error, PlayerMap]] = {
    val maybePlayer = getPlayerOpt(commandContext)
    val owner       = Player.getOfflinePlayer(playerName)
    val canDeleteMap = maybePlayer
      .map(player =>
        owner.getUniqueId == player.getUniqueId || Permission.AdminDeleteMap.isSetOn(commandContext.sender)
      )
      .getOrElse(true)
    EitherT
      .cond[Future](canDeleteMap, (), MapSaverError.NotTheOwner)
      .flatMapF(_ => mapRepository.getMapInfo(owner.getUniqueId, None, name))
      .map(_.playerMap)
      .value
  }
}
