package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.map.ServerMap;
import fr.epicanard.mapsaver.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.List;
import java.util.Optional;

public class ImportCommand implements TabExecutor {

    public MapSaverPlugin plugin;

    public ImportCommand(MapSaverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Optional<ServerMap> maps = this.plugin.getService().getRepository().selectServerMapByLockedIdAndServer(42, "Freeboulde");
        maps.ifPresent(map -> {
            System.out.println(map.getLockedId());
            System.out.println(map.getOriginalId());
            System.out.println(map.getServer());
            System.out.println(map.getMapUuid());
        });
//        if (!(sender instanceof Player)) {
//            sender.sendMessage("Command for player only");
//            return true;
//        }
//
//        if (!this.plugin.byteMap.isPresent()) return true;
//
//        byte[] bytes = this.plugin.byteMap.get();
//        Player player = (Player) sender;
//
//        int id = Bukkit.createMap(Bukkit.getWorlds().get(0)).getId();
//        ItemStack mapItem = createMapItem(id);
//        MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
//        MapView mapView = mapMeta.getMapView();
//
//        mapView.setLocked(true);
//        mapView.getRenderers().forEach(renderer -> {
//            ReflectionUtils.getField(renderer, "worldMap.colors").ifPresent(colors -> {
//                System.arraycopy(bytes, 0, (byte[])colors, 0, bytes.length);
//            });
//        });
//
//        mapMeta.setMapView(mapView);
//        mapItem.setItemMeta(mapMeta);
//
//        player.getInventory().addItem(mapItem);
        return true;
    }

    public static ItemStack createMapItem(int mapID) {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);
        final MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setMapId(mapID);
        mapItem.setItemMeta(meta);
        return mapItem;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
