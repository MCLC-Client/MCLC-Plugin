package de.scholle.mclc.bungee;

import de.scholle.mclc.bungee.serverlist.ServerListPingListener;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class MCLCBungeePlugin extends Plugin {

    @Override
    public void onEnable() {
        int pluginId = 29818;
        new Metrics(this, pluginId);

        saveDefaultConfig();
        getProxy().getPluginManager().registerListener(this, new ServerListPingListener(this));
    }

    private void saveDefaultConfig() {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            getLogger().warning("Datenordner konnte nicht erstellt werden: " + dataFolder.getPath());
            return;
        }

        File configFile = new File(dataFolder, "config.yml");
        if (configFile.exists()) {
            return;
        }

        try (InputStream inputStream = getResourceAsStream("config.yml")) {
            if (inputStream == null) {
                getLogger().warning("config.yml fehlt in den Plugin-Ressourcen");
                return;
            }
            Files.copy(inputStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            getLogger().warning("config.yml konnte nicht geschrieben werden: " + exception.getMessage());
        }
    }
}
