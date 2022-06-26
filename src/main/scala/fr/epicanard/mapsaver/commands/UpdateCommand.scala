package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.commands.UpdateCommand.buildMapToUpdate
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.listeners.SyncListener
import fr.epicanard.mapsaver.map.MapExtractor
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.models.Complete
import fr.epicanard.mapsaver.models.map.MapToUpdate
import fr.epicanard.mapsaver.models.map.status.MapUpdateStatus
import fr.epicanard.mapsaver.resources.language.Help
import org.bukkit.entity.Player

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class UpdateCommand(mapRepository: MapRepository, syncListener: SyncListener)
    extends BaseCommand(Some(Permission.UpdateMap)) {
  def helpMessage(help: Help): String = help.update

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    (for {
      player      <- EitherT.fromEither[Future](CommandContext.getPlayer(commandContext))
      mapToUpdate <- EitherT.fromEither[Future](buildMapToUpdate(player, commandContext))
      result      <- EitherT(mapRepository.updateMap(mapToUpdate))
      _         = syncListener.forwardMessage(player, MapUpdateStatus.dataId(result))
      statusMsg = MapUpdateStatus.getMessage(result, messenger.language.infoMessages)
    } yield msg"$statusMsg").value

  def onTabComplete(commandContext: CommandContext): Future[Either[Error, Complete]] = Complete.Empty.fsuccess
}

object UpdateCommand {
  private def buildMapToUpdate(player: Player, commandContext: CommandContext): Either[Error, MapToUpdate] =
    for {
      mapItem <- MapExtractor.extractFromPlayer(player)
      mapToSave = MapToUpdate(
        id = mapItem.id,
        server = commandContext.server,
        bytes = mapItem.bytes,
        owner = player.getUniqueId
      )
    } yield mapToSave
}
