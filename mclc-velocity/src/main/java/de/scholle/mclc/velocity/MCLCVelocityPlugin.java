package de.scholle.mclc.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import de.scholle.mclc.common.IconFileService;
import de.scholle.mclc.common.ServerListTextService;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Plugin(id = "mclc", name = "MCLC", version = "${version}", authors = { "Mobilestars" })
public final class MCLCVelocityPlugin {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private final ServerListTextService textService;
    private final IconFileService iconFileService;

    @Inject
    public MCLCVelocityPlugin(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.textService = new ServerListTextService();
        this.iconFileService = new IconFileService();

        saveDefaultConfig();
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        try {
            Map<String, Object> config = loadConfig();
            Map<String, Object> serverList = getSection(config, "server-list");
            Map<String, Object> faviconSection = getSection(serverList, "favicon");

            String motd = asString(serverList.get("motd"), "");
            motd = textService.normalizeMotd(motd);

            ServerPing currentPing = event.getPing();
            int online = currentPing.getPlayers().isPresent() ? currentPing.getPlayers().get().getOnline()
                    : proxyServer.getPlayerCount();
            int maxPlayers = currentPing.getPlayers().isPresent() ? currentPing.getPlayers().get().getMax() : online;
            String version = currentPing.getVersion().getName();

            Map<String, String> placeholders = new HashMap<String, String>();
            placeholders.put("%online%", String.valueOf(online));
            placeholders.put("%max_players%", String.valueOf(maxPlayers));
            placeholders.put("%version%", version);
            motd = textService.applyPlaceholders(motd, placeholders);

            ServerPing.Builder builder = currentPing.asBuilder();
            builder.description(LegacyComponentSerializer.legacyAmpersand().deserialize(motd));

            boolean iconEnabled = asBoolean(faviconSection.get("enabled"), true);
            if (iconEnabled) {
                String iconPath = asString(faviconSection.get("path"), "server-icon.png");
                File dataFolder = dataDirectory.toFile();
                File iconFile = iconFileService.resolveIconFile(dataFolder, iconPath);
                if (iconFile.exists() && iconFile.isFile()) {
                    File icon64 = iconFileService.ensure64x64Png(iconFile, dataFolder, "server-icon-resized.png");
                    builder.favicon(Favicon.create(icon64.toPath()));
                }
            }

            event.setPing(builder.build());
        } catch (Exception exception) {
            logger.warn("Serverlist-Ping konnte nicht verarbeitet werden: {}", exception.getMessage());
        }
    }

    private void saveDefaultConfig() {
        File dataFolder = dataDirectory.toFile();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            logger.warn("Datenordner konnte nicht erstellt werden: {}", dataFolder.getPath());
            return;
        }

        File configFile = new File(dataFolder, "config.yml");
        if (configFile.exists()) {
            return;
        }

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            if (inputStream == null) {
                logger.warn("config.yml fehlt in den Plugin-Ressourcen");
                return;
            }
            Files.copy(inputStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            logger.warn("config.yml konnte nicht geschrieben werden: {}", exception.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadConfig() throws IOException {
        File configFile = new File(dataDirectory.toFile(), "config.yml");
        if (!configFile.exists()) {
            return new HashMap<String, Object>();
        }

        Yaml yaml = new Yaml();
        try (InputStream inputStream = Files.newInputStream(configFile.toPath())) {
            Object loaded = yaml.load(inputStream);
            if (loaded instanceof Map) {
                return (Map<String, Object>) loaded;
            }
            return new HashMap<String, Object>();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getSection(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return new HashMap<String, Object>();
    }

    private String asString(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private boolean asBoolean(Object value, boolean fallback) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return fallback;
    }
}
