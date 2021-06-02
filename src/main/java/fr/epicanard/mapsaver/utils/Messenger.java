package fr.epicanard.mapsaver.utils;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messenger {
    public static String prefix;

    public static void setPrefix(final String prefix) {
        Messenger.prefix = toColor(prefix);
    }

    public static void sendMessage(final CommandSender sender, final String message) {
        sender.sendMessage(prefix + toColor(message));
    }

    public static void sendMessage(final CommandSender sender, final TextComponent message) {
        sender.spigot().sendMessage(message);
    }

    public static void sendMessage(final Player player, final String message) {
        player.sendMessage(toColor(message));
    }

    public static String toColor(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
