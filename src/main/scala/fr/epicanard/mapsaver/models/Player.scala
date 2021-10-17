package fr.epicanard.mapsaver.models

import org.bukkit.Bukkit
import org.bukkit.{OfflinePlayer => BOfflinePlayer}

import java.util.UUID
import scala.annotation.nowarn

object Player {

  @nowarn
  def getOfflinePlayer(name: String): BOfflinePlayer = Bukkit.getOfflinePlayer(name)

  def getOfflinePlayer(uuid: UUID): BOfflinePlayer = Bukkit.getOfflinePlayer(uuid)

}
