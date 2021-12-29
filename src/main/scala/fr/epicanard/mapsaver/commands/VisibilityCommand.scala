package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import cats.implicits._
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.commands.VisibilityCommand._
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.{MissingMapName, WrongVisibility}
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.models.map.Visibility
import fr.epicanard.mapsaver.resources.language.Help
import fr.epicanard.mapsaver.map.MapExtractor
import fr.epicanard.mapsaver.models.UpdateVisibility
import fr.epicanard.mapsaver.models.Player

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.util.UUID

case class VisibilityCommand(mapRepository: MapRepository) extends BaseCommand(Some(Permission.VisibilityMap)) {
  def helpMessage(help: Help): String = help.visibility

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    (for {
      request <- EitherT.fromEither[Future](getUpdateVisbility(commandContext))
      _       <- EitherT(mapRepository.updateVisibility(request))
      statusMsg = messenger.language.infoMessages.visibilityMapUpdated
    } yield msg"$statusMsg").value

  def onTabComplete(commandContext: CommandContext): List[String] = Nil

  def getUpdateVisbility(commandContext: CommandContext): Either[Error, UpdateVisibility] =
    parseArgs(commandContext)
      .flatMap { case (info, player, vis) =>
        parseVisibility(vis)
          .map { visibility =>
            val canSetVisibility: UUID => Boolean = owner =>
              CommandContext
                .getPlayerOpt(commandContext)
                .exists(sender => sender.getUniqueId == owner) || Permission.AdminVisibilityMap.isSetOn(
                commandContext.sender
              )
            UpdateVisibility(
              canSetVisibility,
              player.getUniqueId(),
              commandContext.config.serverName,
              visibility,
              info
            )
          }
      }
}

object VisibilityCommand {
  private def parseArgs(commandContext: CommandContext) =
    commandContext.args match {
      case mapName :: playerName :: visibility :: _ =>
        Right((UpdateVisibility.InfoMapName(mapName), Player.getOfflinePlayer(playerName), visibility))
      case mapName :: visibility :: _ =>
        CommandContext
          .getPlayer(commandContext)
          .map(player => (UpdateVisibility.InfoMapName(mapName), player, visibility))
      case visibility :: _ =>
        for {
          player <- CommandContext.getPlayer(commandContext)
          map    <- MapExtractor.extractMapView(player)
        } yield (UpdateVisibility.InfoMapId(map.getId()), player, visibility)
      case Nil => Left(MissingMapName)
    }

  private def parseVisibility(visibility: String): Either[WrongVisibility, Visibility] =
    Visibility.withNameInsensitiveOption(visibility).toRight(WrongVisibility(visibility))
}
