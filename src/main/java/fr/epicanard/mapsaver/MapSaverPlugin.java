package fr.epicanard.mapsaver;

import fr.epicanard.duckconfig.DuckLoader;
import fr.epicanard.duckconfig.annotations.ResourceWrapper;
import fr.epicanard.mapsaver.command.MapSaverCommand;
import fr.epicanard.mapsaver.config.Config;
import fr.epicanard.mapsaver.database.MapRepository;
import fr.epicanard.mapsaver.language.Language;
import fr.epicanard.mapsaver.services.MapService;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.logging.Level;

public class MapSaverPlugin extends JavaPlugin {

    public Optional<byte[]> byteMap = Optional.empty();
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

        final MapRepository repository = new MapRepository(this);
        repository.setupDatabase();
        this.service = new MapService(this, repository);

        this.getCommand("mapsaver").setExecutor(new MapSaverCommand(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabling MapSaver...");
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
        this.getLogger().log(Level.WARNING, "Plugin GlobalMarketChest disabled");
    }
}
