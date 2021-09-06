package fr.epicanard.mapsaver

import fr.epicanard.mapsaver.resources.language.{ErrorMessages, Help, Language}
import org.bukkit.command.CommandSender

case class Messenger(prefix: String, language: Language) {
  def sendError(sender: CommandSender)(getMessage: ErrorMessages => String): Unit =
    send(sender, s"&c${getMessage(language.errorMessages)}")

  def sendHelp(sender: CommandSender)(getMessage: Help => String): Unit =
    send(sender, getMessage(language.help))

  private def send(sender: CommandSender, message: String): Unit =
    sender.sendMessage(toColor(s"$prefix$message"))

  private def toColor(message: String): String =
    message.replaceAll("&", "§")
}
