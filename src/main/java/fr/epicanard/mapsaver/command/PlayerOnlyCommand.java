package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public abstract class PlayerOnlyCommand extends BaseCommand {
    public PlayerOnlyCommand(MapSaverPlugin plugin) {
        super(plugin);
    }

    public boolean isPlayerOnly(final CommandSender sender, final String[] args) {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyList();
    }
}
