package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class VersionCommand extends BaseCommand {

    public VersionCommand(MapSaverPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sendMessage(sender, plugin.getDescription().getVersion());

        return true;
    }
}
