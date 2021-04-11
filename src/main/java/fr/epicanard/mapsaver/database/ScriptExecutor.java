package fr.epicanard.mapsaver.database;

import fr.epicanard.mapsaver.MapSaverPlugin;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class ScriptExecutor {
    /**
     * Execute sql script file
     *
     * @param plugin MapSaverPlugin
     * @param dataSource Connection to database
     * @param script Name of script to execute
     */
    public static void executeScriptFile(final MapSaverPlugin plugin, final DataSource dataSource, final String script) {
        final List<String> queries = readScriptFile(
                plugin,
                String.format("scripts/%s/%s", plugin.getConfiguration().Storage.Type, script)
        );

        try {
            final Statement prepared = dataSource.getConnection().createStatement();
            prepared.closeOnCompletion();
            for (String query : queries) {
                prepared.addBatch(query);
            }
            prepared.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read a script file and split it with ';'.
     *
     * @param plugin MapSaverPlugin
     * @param path Path to the script file
     * @return List of request to execute
     */
    public static List<String> readScriptFile(final MapSaverPlugin plugin, final String path) {
        final InputStream stream = plugin.getResource(path);

        try {
            if (stream != null) {
                String file = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"))
                        .replaceAll("(\\{prefix})", plugin.getConfiguration().Storage.TablePrefix);
                return Arrays.stream(file.split(";"))
                        .filter(line -> line.trim().length() > 0)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
