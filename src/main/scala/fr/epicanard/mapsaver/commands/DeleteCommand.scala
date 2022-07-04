package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.PlayerOnlyCommand
import fr.epicanard.mapsaver.map.MapExtractor
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.models.{Complete, MapIdentifier, Player}
import fr.epicanard.mapsaver.resources.language.Help
import org.bukkit.entity

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class DeleteCommand(mapRepository: MapRepository) extends BaseCommand(Some(Permission.DeleteMap)) {

  def helpMessage(help: Help): String = help.delete

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] = {
    val maybePlayer = CommandContext.getPlayerOpt(commandContext)
    (for {
      identifier <- EitherT.fromEither[Future](DeleteCommand.parseIdentifier(maybePlayer, commandContext))
      _ <- EitherT(
        mapRepository.deletePlayerMap(identifier)(owner =>
          maybePlayer.forall(player => player.getUniqueId() == owner || Permission.AdminDeleteMap.isSetOn(player))
        )
      )
    } yield msg"""${messenger.language.infoMessages.mapDeleted}""").value
  }

  def onTabComplete(commandContext: CommandContext): Future[Either[Error, Complete]] =
    BaseCommand.mapTabComplete(mapRepository, commandContext)
}

object DeleteCommand {
  private def parseIdentifier(
      maybePlayer: Option[entity.Player],
      commandContext: CommandContext
  ): Either[Error, MapIdentifier] =
    commandContext.args match {
      case playerName :: mapName :: _ =>
        Right(MapIdentifier.MapName(mapName, Player.getOfflinePlayer(playerName).getUniqueId()))
      case mapName :: _ =>
        maybePlayer
          .toRight(PlayerOnlyCommand)
          .map(player => MapIdentifier.MapName(mapName, player.getUniqueId()))
      case Nil =>
        for {
          player <- maybePlayer.toRight(PlayerOnlyCommand)
          map    <- MapExtractor.extractMapView(player)
        } yield MapIdentifier.MapId(map.getId(), commandContext.server)
    }

}
