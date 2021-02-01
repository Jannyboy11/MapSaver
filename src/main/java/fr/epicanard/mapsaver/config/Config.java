package fr.epicanard.mapsaver.config;

import fr.epicanard.duckconfig.annotations.Header;
import fr.epicanard.duckconfig.annotations.Resource;
import fr.epicanard.duckconfig.annotations.ResourceLocation;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

//@Resource(value = "config.yml", location = ResourceLocation.FILE_PATH)
@Header({
        "==============",
        "Config file for plugin MapSaver",
        "=============="
})
@AllArgsConstructor
@NoArgsConstructor
public class Config {
    public String language = "en_US";
    public String plop = "tata";

    @Override
    public String toString() {
        return String.format("[language:%s, plop:%s]", language, plop);
    }

}
