package de.scholle.mclc.bukkit.serverlist;

import de.scholle.mclc.bukkit.serverlist.listener.ServerListPingListener;
import de.scholle.mclc.bukkit.serverlist.service.FaviconService;
import de.scholle.mclc.bukkit.serverlist.service.PlaceholderService;
import de.scholle.mclc.common.ServerListTextService;
import org.bukkit.plugin.java.JavaPlugin;

public final class ServerListBootstrap {

    private final JavaPlugin plugin;

    public ServerListBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        PlaceholderService placeholderService = new PlaceholderService(plugin);
        FaviconService faviconService = new FaviconService(plugin);
        ServerListTextService textService = new ServerListTextService();

        faviconService.reloadFromConfig();

        ServerListPingListener pingListener = new ServerListPingListener(plugin, placeholderService, faviconService,
                textService);
        plugin.getServer().getPluginManager().registerEvents(pingListener, plugin);
    }
}
