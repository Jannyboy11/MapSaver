package fr.epicanard.mapsaver.models.language;

import fr.epicanard.duckconfig.annotations.Header;

import java.util.Map;

@Header({
        "==============",
        "Language file for plugin MapSaver",
        "=============="
})
public class Language {
    public Help Help;
    public ErrorMessages ErrorMessages;
    public InfoMessages InfoMessages;
    public MapInfo MapInfo;
    public List List;
    public Map<String, String> Visibility;
}
