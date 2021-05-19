package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.models.map.PlayerMap;
import fr.epicanard.mapsaver.models.map.Visibility;
import fr.epicanard.mapsaver.models.Permission;
import fr.epicanard.mapsaver.utils.Messenger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static fr.epicanard.mapsaver.utils.Messenger.createLink;
import static fr.epicanard.mapsaver.utils.Messenger.newComponent;
import static fr.epicanard.mapsaver.utils.Messenger.toColor;

public class ListCommand extends PlayerOnlyCommand {

    public ListCommand(MapSaverPlugin plugin) {
        super(plugin, Permission.LIST_MAP, plugin.getLanguage().Help.List);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final TextComponent message = newComponent(plugin.getLanguage().List.ListMaps);

        listPlayerMaps(args, sender).forEach(map -> {
            final String visibilityText = plugin.getLanguage().Visibility.getOrDefault(map.getVisibility().name(), map.getVisibility().name());
            final TextComponent line = newComponent(" • &6%s&f - %s%s&f", map.getName(), getVisibilityColor(map.getVisibility()), visibilityText);

            if (Permission.INFO_MAP.isSetOn(sender) || Permission.LIST_MAP.isSetOn(sender)) {
                line.addExtra(" - ");
                if (Permission.INFO_MAP.isSetOn(sender)) {
                    line.addExtra(createLink("info", plugin.getLanguage().List.InfoHover, ChatColor.DARK_GREEN, String.format("/mapsaver info %s %s", map.getName(), (args.length > 0) ? args[0] : "")));
                    line.addExtra(toColor("&7/"));
                }
                if (Permission.IMPORT_MAP.isSetOn(sender)) {
                    line.addExtra(createLink("import", plugin.getLanguage().List.ImportHover, ChatColor.DARK_GREEN, String.format("/mapsaver import %s %s", map.getName(), (args.length > 0) ? args[0] : "")));
                }
            }

            message.addExtra("\n");
            message.addExtra(line);
        });
        Messenger.sendMessage(sender, message);

        return true;
    }

    @Override
    public boolean isPlayerOnly(final CommandSender sender, final String[] args) {
        return (args.length >= 1) || super.isPlayerOnly(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        return null;
    }

    /*
     * 1. If console Return all maps
     * 2. If owner Return all maps
     * 3. If admin permission Return all maps
     * 4. If not owner Return public maps only
     */
    private List<PlayerMap> listPlayerMaps(final String[] args, final CommandSender sender) {
        UUID playerUuid;
        if (args.length > 0) {
            playerUuid = plugin.getServer().getOfflinePlayer(args[0]).getUniqueId();
        } else {
            playerUuid = ((Player) sender).getUniqueId();
        }

        if (!(sender instanceof Player) || playerUuid == ((Player) sender).getUniqueId() || Permission.ADMIN_LIST_MAP.isSetOn(sender)) {
            return this.plugin.getService().listAllPlayerMaps(playerUuid);
        }
        return this.plugin.getService().listPublicPlayerMaps(playerUuid);
    }

    private String getVisibilityColor(final Visibility visibility) {
        switch (visibility) {
            case PUBLIC:
                return "&7";
            case PRIVATE:
                return "&8";
            default:
                return "&f";
        }
    }
}