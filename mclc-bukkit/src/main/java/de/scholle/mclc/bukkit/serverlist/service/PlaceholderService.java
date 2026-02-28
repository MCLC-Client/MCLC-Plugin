package de.scholle.mclc.bukkit.serverlist.service;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public final class PlaceholderService {

    private final boolean placeholderApiEnabled;

    public PlaceholderService(JavaPlugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        this.placeholderApiEnabled = pluginManager.isPluginEnabled("PlaceholderAPI");
    }

    public String applyPlaceholders(OfflinePlayer offlinePlayer, String text) {
        if (!placeholderApiEnabled || text == null || text.isEmpty()) {
            return text;
        }

        try {
            Class<?> placeholderApiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Method method = placeholderApiClass.getMethod("setPlaceholders", OfflinePlayer.class, String.class);
            return (String) method.invoke(null, offlinePlayer, text);
        } catch (ReflectiveOperationException ignored) {
            return text;
        }
    }
}
