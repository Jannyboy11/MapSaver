package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.permission.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static fr.epicanard.mapsaver.utils.MapUtils.extractMapToSaveFromPlayer;
import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class SaveCommand extends PlayerOnlyCommand {

    public SaveCommand(MapSaverPlugin plugin) {
        super(plugin, Permissions.SAVE_MAP);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String mapName = (args.length >= 1) ? args[0] : "default";

        extractMapToSaveFromPlayer(plugin, (Player) sender, mapName)
            .match(
                left  -> sendMessage(sender, left),
                right -> this.plugin.getService().saveMap(right, sender)
            );

        return true;
    }
}
