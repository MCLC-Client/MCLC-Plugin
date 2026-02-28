package de.scholle.mclc.bukkit.serverlist.service;

import de.scholle.mclc.common.IconFileService;
import org.bukkit.Bukkit;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;

import java.io.File;

public final class FaviconService {

    private final JavaPlugin plugin;
    private final IconFileService iconFileService;
    private CachedServerIcon cachedServerIcon;

    public FaviconService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.iconFileService = new IconFileService();
    }

    public void reloadFromConfig() {
        boolean enabled = plugin.getConfig().getBoolean("server-list.favicon.enabled", true);
        if (!enabled) {
            cachedServerIcon = null;
            return;
        }

        String configuredPath = plugin.getConfig().getString("server-list.favicon.path", "server-icon.png");
        if (configuredPath == null || configuredPath.trim().isEmpty()) {
            cachedServerIcon = null;
            return;
        }

        File iconFile = iconFileService.resolveIconFile(plugin.getDataFolder(), configuredPath.trim());
        if (!iconFile.exists() || !iconFile.isFile()) {
            plugin.getLogger().warning("Serverlist-Favicon nicht gefunden: " + iconFile.getPath());
            cachedServerIcon = null;
            return;
        }

        try {
            File resizedFile = iconFileService.ensure64x64Png(iconFile, plugin.getDataFolder(),
                    "server-icon-resized.png");
            cachedServerIcon = Bukkit.loadServerIcon(resizedFile);
        } catch (Exception exception) {
            plugin.getLogger().warning("Serverlist-Favicon konnte nicht geladen werden: " + exception.getMessage());
            cachedServerIcon = null;
        }
    }

    public void applyIcon(ServerListPingEvent event) {
        if (cachedServerIcon != null) {
            event.setServerIcon(cachedServerIcon);
        }
    }
}
