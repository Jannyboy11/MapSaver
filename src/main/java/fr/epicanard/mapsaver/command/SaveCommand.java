package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.utils.ReflectionUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.Optional;

public class SaveCommand implements CommandExecutor {

    public MapSaverPlugin plugin;

    public SaveCommand(MapSaverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Command for player only");
            return true;
        }

        Player player = (Player) sender;
        ItemStack stack = player.getInventory().getItemInMainHand();

        if (stack.getType() != Material.FILLED_MAP) {
            sender.sendMessage("You need to have a filled map in your hand");
            return true;
        }

        if (!(stack.getItemMeta() instanceof MapMeta)) {
            sender.sendMessage("Error getting map Meta");
            this.plugin.getLogger().warning("Player: " + player.getName() + " tried to save " + stack.getType().toString() + " which was considered a map, but itemMeta is not valid!");
            return true;
        }

        MapMeta mapMeta = (MapMeta) stack.getItemMeta();
        MapView mapView = mapMeta.getMapView();


        final Optional<MapRenderer> renderer = mapView.getRenderers().stream().findFirst();
        if (renderer.isPresent()) {
            final byte[] byteMap =  new byte[16384];;
            ReflectionUtils.getField(renderer, "worldMap.colors").ifPresent(colors -> {
                System.arraycopy((byte[])colors, 0, byteMap, 0, 16384);
                this.plugin.byteMap = Optional.of(byteMap);
            });
        }

        return true;
    }
}
