package de.scholle.mclc.bukkit;

import de.scholle.mclc.bukkit.serverlist.ServerListBootstrap;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class MCLCBukkitPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        int pluginId = 29817;
        new Metrics(this, pluginId);
        saveDefaultConfig();
        new ServerListBootstrap(this).enable();
    }
}
