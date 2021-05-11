package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.map.Visibility;
import fr.epicanard.mapsaver.permission.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

import static fr.epicanard.mapsaver.utils.MapUtils.extractMapToSaveFromPlayer;
import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class SaveCommand extends PlayerOnlyCommand {

    public SaveCommand(MapSaverPlugin plugin) {
        super(plugin, Permissions.SAVE_MAP, plugin.getLanguage().Help.Save);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String mapName = (args.length >= 1) ? args[0] : "default";
        final Visibility visibility = Optional
            .ofNullable((args.length >= 2) ? args[1] : null)
            .flatMap(Visibility::find)
            .orElse(plugin.getConfiguration().Privacy.DefaultVisibility);

        extractMapToSaveFromPlayer(plugin, (Player) sender, mapName, visibility)
            .match(
                left  -> sendMessage(sender, left),
                right -> this.plugin.getService().saveMap(right, sender)
            );

        return true;
    }
}
