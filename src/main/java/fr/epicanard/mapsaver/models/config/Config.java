package fr.epicanard.mapsaver.models.config;

import fr.epicanard.duckconfig.annotations.Header;
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
