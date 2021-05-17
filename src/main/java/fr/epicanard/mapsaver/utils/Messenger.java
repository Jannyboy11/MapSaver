package fr.epicanard.mapsaver.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messenger {
    private static String prefix;

    public static void setPrefix(final String prefix) {
        Messenger.prefix = prefix;
    }

    public static void sendMessage(final CommandSender sender, final String message) {
        sender.sendMessage(toColor(prefix + message));
    }

    public static void sendMessage(final Player player, final String message) {
        player.sendMessage(toColor(message));
    }

    public static String toColor(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
