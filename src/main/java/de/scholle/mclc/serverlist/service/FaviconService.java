package de.scholle.mclc.serverlist.service;

import org.bukkit.Bukkit;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class FaviconService {

    private final JavaPlugin plugin;
    private CachedServerIcon cachedServerIcon;

    public FaviconService(JavaPlugin plugin) {
        this.plugin = plugin;
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

        File iconFile = resolveIconFile(configuredPath.trim());
        if (!iconFile.exists() || !iconFile.isFile()) {
            plugin.getLogger().warning("Serverlist-Favicon nicht gefunden: " + iconFile.getPath());
            cachedServerIcon = null;
            return;
        }

        try {
            File resizedFile = ensureResizedIcon(iconFile);
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

    private File resolveIconFile(String configuredPath) {
        File configuredFile = new File(configuredPath);
        if (configuredFile.isAbsolute()) {
            return configuredFile;
        }

        File dataFolderFile = new File(plugin.getDataFolder(), configuredPath);
        if (dataFolderFile.exists()) {
            return dataFolderFile;
        }

        File serverRootFile = new File(configuredPath);
        if (serverRootFile.exists()) {
            return serverRootFile;
        }

        return dataFolderFile;
    }

    private File ensureResizedIcon(File sourceFile) throws IOException {
        BufferedImage sourceImage = ImageIO.read(sourceFile);
        if (sourceImage == null) {
            throw new IOException("Datei ist kein lesbares Bild: " + sourceFile.getPath());
        }

        if (sourceImage.getWidth() == 64 && sourceImage.getHeight() == 64) {
            return sourceFile;
        }

        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new IOException("Plugin-Datenordner konnte nicht erstellt werden");
        }

        BufferedImage resizedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(sourceImage, 0, 0, 64, 64, null);
        graphics.dispose();

        File resizedFile = new File(plugin.getDataFolder(), "server-icon-resized.png");
        ImageIO.write(resizedImage, "png", resizedFile);
        return resizedFile;
    }
}
