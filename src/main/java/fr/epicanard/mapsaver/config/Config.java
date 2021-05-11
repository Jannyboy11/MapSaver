package fr.epicanard.mapsaver.config;

import fr.epicanard.duckconfig.annotations.Header;
import fr.epicanard.mapsaver.map.Visibility;
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
    public String Language = "en_US";
    public String Prefix = "&a[MapSaver] &f";
    public String ServerName = "server";
    public Storage Storage;
    public Privacy Privacy;
}
