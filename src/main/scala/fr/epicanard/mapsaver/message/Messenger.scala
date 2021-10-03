package fr.epicanard.mapsaver.message

import fr.epicanard.mapsaver.resources.language.{ErrorMessages, Help, Language}
import org.bukkit.command.CommandSender

import java.util.logging.Logger

case class Messenger(prefix: String, language: Language, logger: Logger) {
  def sendError(sender: CommandSender, getMessage: ErrorMessages => String): Unit =
    sendToSender(sender, s"&c${getMessage(language.errorMessages)}")

  def sendHelp(sender: CommandSender, getMessage: Help => String): Unit =
    sendToSender(sender, getMessage(language.help))

  def logWarn(message: String): Unit =
    logger.warning(message)

  def sendToSender(sender: CommandSender, message: String): Unit =
    sender.sendMessage(Component.toColor(s"$prefix$message"))

  def sendToSender(sender: CommandSender, message: Component): Unit =
    sender.spigot().sendMessage(message.value)

  def sendAllToSender(sender: CommandSender, message: Message): Unit =
    message.withPrefix(prefix).components.foreach(sendToSender(sender, _))

}
