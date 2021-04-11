package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import org.bukkit.command.CommandSender;

public abstract class PlayerOnlyCommand extends BaseCommand {
    public PlayerOnlyCommand(MapSaverPlugin plugin) {
        super(plugin);
    }

    public boolean isPlayerOnly(final CommandSender sender, final String[] args) {
        return true;
    }
}
