package fr.epicanard.mapsaver.permission;

import fr.epicanard.mapsaver.MapSaverPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 * Class that handle permissions of plugin
 */
public enum Permissions {
    SAVE_MAP("mapsaver.commands.save"),
    UPDATE_MAP("mapsaver.commands.update"),
    IMPORT_MAP("mapsaver.commands.import"),
    LIST_MAP("mapsaver.commands.list"),
    RELOAD("mapsaver.admin.commands.reload"),
    ADMIN_IMPORT_MAP("mapsaver.admin.commands.import"),
    ;

    private final String permission;

    /**
     * Enum constructor
     * @param permission String permission
     */
    Permissions(String permission) {
        this.permission = permission;
    }

    /**
     * Define if the permission is set
     *
     * @param player Player on which check the permissions
     * @return Return a boolean to define if the permission is set
     */
    public Boolean isSetOn(Player player) {
        return player != null && player.hasPermission(this.permission);
    }

    /**
     * Define if the permission is set on the CommandSender
     * If the CommandSender is not a Player (ex: console) the result is set with the value
     * of param defaultSender
     *
     * @param sender CommandSender on which check the permissions
     * @param defaultSender Value returned if sender is not Player
     * @return Return a boolean to define if the permission is set
     */
    public Boolean isSetOn(CommandSender sender, Boolean defaultSender) {
        if (sender instanceof Player)
            return this.isSetOn((Player) sender);
        return defaultSender;
    }

    /**
     * Define if the permission is set on the CommandSender
     * If the CommandSender is not a Player (ex: console) the result is set to true
     *
     * @param sender CommandSender on which check the permissions
     * @return Return a boolean to define if the permission is set
     */
    public Boolean isSetOn(CommandSender sender) {
        return this.isSetOn(sender, true);
    }

    /**
     * Define if the permission is set and print an error message
     *
     * @param player Player on which check the permissions
     * @return Return a boolean to define if the permission is set
     */
    public Boolean isSetOnWithMessage(MapSaverPlugin plugin, Player player) {
        final Boolean isSet = this.isSetOn(player);

        if (!isSet) {
            Permissions.sendMessage(plugin, player);
        }

        return isSet;
    }

    /**
     * Send permission denied to the player
     *
     * @param player Player that must receive the message
     */
    public static void sendMessage(MapSaverPlugin plugin, Player player) {
        player.sendMessage(plugin.getLanguage().ErrorMessages.PermissionNotAllowed);
    }
}
