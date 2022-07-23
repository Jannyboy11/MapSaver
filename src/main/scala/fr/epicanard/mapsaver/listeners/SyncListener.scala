package fr.epicanard.mapsaver.listeners

import cats.data.EitherT
import com.google.common.io.ByteStreams
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error.handleTryResult
import fr.epicanard.mapsaver.errors.TechnicalError
import fr.epicanard.mapsaver.map.BukkitMapBuilder.MapViewBuilder
import fr.epicanard.mapsaver.message.Messenger
import fr.epicanard.mapsaver.models.map.MapViewWithData
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.PluginMessageListener

import scala.concurrent.ExecutionContext

class SyncListener(plugin: Plugin, mapRepository: MapRepository, messenger: Messenger, serverName: String)(implicit
    ec: ExecutionContext
) extends PluginMessageListener {

  override def onPluginMessageReceived(channel: String, player: Player, message: Array[Byte]): Unit = {
    val input      = ByteStreams.newDataInput(message)
    val subchannel = input.readUTF()

    if (subchannel == "MapSaver") {
      val len = input.readShort()
      val arr = new Array[Byte](len)
      input.readFully(arr)
      val in     = ByteStreams.newDataInput(arr)
      val dataId = in.readInt()
      EitherT(mapRepository.findMapViewWithData(dataId, serverName))
        .subflatMap { case MapViewWithData(mapView, data) => MapViewBuilder.updateMapColors(mapView, data.bytes) }
        .value
        .onComplete {
          handleTryResult(_) match {
            case Left(error: TechnicalError) => TechnicalError.logError(error, messenger.logger)
            case Left(_)                     => ()
            case Right(mapView) =>
              messenger.sendToSender(
                plugin.getServer().getConsoleSender(),
                s"[Synced] MapId: ${mapView.getId()}, DataId: $dataId"
              )
          }
        }
    }
  }

  def forwardMessage(player: Player, dataId: Int): Unit = {
    val output = ByteStreams.newDataOutput()
    val msg    = ByteStreams.newDataOutput()

    output.writeUTF("Forward")
    output.writeUTF("ALL")
    output.writeUTF("MapSaver")

    msg.writeInt(dataId)
    output.writeShort(msg.toByteArray().length)
    output.write(msg.toByteArray())
    player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
  }

}
