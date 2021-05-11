package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.map.Visibility;
import fr.epicanard.mapsaver.permission.Permissions;
import fr.epicanard.mapsaver.utils.OptionalUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.epicanard.mapsaver.utils.MapUtils.extractMapToSaveFromPlayer;
import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class UpdateCommand extends PlayerOnlyCommand {

    public UpdateCommand(MapSaverPlugin plugin) {
        super(plugin, Permissions.UPDATE_MAP, plugin.getLanguage().Help.Update);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Visibility visibility = null;

        if (args.length >= 1 ) {
            visibility = getValidity(sender, args[0]).orElse(null);
            if (visibility == null) {
                return true;
            }
        }

        extractMapToSaveFromPlayer(plugin, (Player) sender, null, visibility)
            .match(
                left  -> sendMessage(sender, left),
                right -> this.plugin.getService().updateMap(right, sender)
            );

        return true;
    }

    public Optional<Visibility> getValidity(CommandSender sender, String arg) {
        return OptionalUtils.ifEmpty(Visibility.find(arg), () ->
                sendMessage(sender, String.format(plugin.getLanguage().ErrorMessages.WrongVisibility, arg, Arrays
                    .stream(Visibility.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "))))
            );
    }
}
