package fr.epicanard.mapsaver;

import fr.epicanard.duckconfig.DuckLoader;
import fr.epicanard.duckconfig.annotations.ResourceWrapper;
import fr.epicanard.mapsaver.command.MapSaverCommand;
import fr.epicanard.mapsaver.database.MapRepository;
import fr.epicanard.mapsaver.models.config.Config;
import fr.epicanard.mapsaver.models.language.Language;
import fr.epicanard.mapsaver.services.MapService;
import fr.epicanard.mapsaver.utils.Messenger;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class MapSaverPlugin extends JavaPlugin {

    @Getter
    private Config configuration;
    @Getter
    private Language language;
    @Getter
    private MapService service;

    @Override
    public void onEnable() {
        this.getLogger().info("Enabling MapSaver...");

        this.configuration = this.loadFile(Config.class, "config.yml");
        this.language = this.loadFile(Language.class, String.format("langs/%s.yml", this.configuration.Language));

        Messenger.setPrefix(this.configuration.Prefix);

        try {
            final MapRepository repository = new MapRepository(this);
            repository.setupDatabase();
            this.service = new MapService(this, repository);
        } catch (Exception e) {
            this.getLogger().severe("Impossible to init database. " + e.getMessage());
            this.disable();
            return;
        }

        this.getCommand("mapsaver").setExecutor(new MapSaverCommand(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabling MapSaver...");
        this.configuration = null;
        this.language = null;
        this.service = null;
        HandlerList.unregisterAll(this);
    }

    /**
     * Save a resource from classpath and load it to a class
     *
     * @param <T> Type expected in return
     * @param clazz Class of expected type
     * @param resourcePath Path to resource to save and load
     * @return The class loaded from plugin folder
     */
    private <T> T loadFile(Class<T> clazz, String resourcePath) {
      if (this.getClassLoader().getResource(resourcePath) != null) {
        this.saveResource(resourcePath, false);
      }
      return DuckLoader.load(clazz, new ResourceWrapper(this.getDataFolder().getPath(), resourcePath));
    }

    /**
     * Disable the plugin
     */
    private void disable() {
        this.setEnabled(false);
        this.getLogger().warning("Plugin MapSaver force disabled");
    }
}
