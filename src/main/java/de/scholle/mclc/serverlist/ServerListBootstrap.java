package de.scholle.mclc.serverlist;

import de.scholle.mclc.serverlist.listener.ServerListPingListener;
import de.scholle.mclc.serverlist.service.FaviconService;
import de.scholle.mclc.serverlist.service.PlaceholderService;
import org.bukkit.plugin.java.JavaPlugin;

public final class ServerListBootstrap {

    private final JavaPlugin plugin;

    public ServerListBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        PlaceholderService placeholderService = new PlaceholderService(plugin);
        FaviconService faviconService = new FaviconService(plugin);
        faviconService.reloadFromConfig();

        ServerListPingListener pingListener = new ServerListPingListener(plugin, placeholderService, faviconService);
        plugin.getServer().getPluginManager().registerEvents(pingListener, plugin);
    }
}
