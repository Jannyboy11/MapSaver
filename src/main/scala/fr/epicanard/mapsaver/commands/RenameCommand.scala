package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.{MissingMapName, PlayerOnlyCommand}
import fr.epicanard.mapsaver.map.MapExtractor
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.models.{Complete, MapIdentifier, Player}
import fr.epicanard.mapsaver.resources.language.Help
import org.bukkit.entity

import scala.concurrent.{ExecutionContext, Future}

case class RenameCommand(mapRepository: MapRepository)(implicit ec: ExecutionContext)
    extends BaseCommand(Some(Permission.RenameMap)) {

  def helpMessage(help: Help): String = help.rename

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] = {
    val maybePlayer = CommandContext.getPlayerOpt(commandContext)
    (for {
      identifierWithName <- EitherT.fromEither[Future](RenameCommand.parseIdentifier(maybePlayer, commandContext))
      _ <- EitherT(
        mapRepository.renamePlayerMap(identifierWithName.identifier, identifierWithName.name)(owner =>
          maybePlayer.forall(player => player.getUniqueId() == owner || Permission.AdminRenameMap.isSetOn(player))
        )
      )
      message = messenger.language.infoMessages.mapRenamed.format(identifierWithName.name)
    } yield msg"$message").value
  }

  def onTabComplete(commandContext: CommandContext): Future[Either[Error, Complete]] =
    BaseCommand.mapTabComplete(mapRepository, commandContext)
}

object RenameCommand {
  private case class IdentifierWithName(identifier: MapIdentifier, name: String)

  private def parseIdentifier(
      maybePlayer: Option[entity.Player],
      commandContext: CommandContext
  ): Either[Error, IdentifierWithName] =
    commandContext.args match {
      case playerName :: oldName :: newName :: _ =>
        Right(
          IdentifierWithName(MapIdentifier.MapName(oldName, Player.getOfflinePlayer(playerName).getUniqueId()), newName)
        )
      case mapName :: newName :: Nil =>
        maybePlayer
          .toRight(PlayerOnlyCommand)
          .map(player => IdentifierWithName(MapIdentifier.MapName(mapName, player.getUniqueId()), newName))
      case newName :: Nil =>
        for {
          player <- maybePlayer.toRight(PlayerOnlyCommand)
          map    <- MapExtractor.extractMapView(player)
        } yield IdentifierWithName(MapIdentifier.MapId(map.getId(), commandContext.server), newName)
      case Nil => Left(MissingMapName)
    }
}
