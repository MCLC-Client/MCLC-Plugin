package de.scholle.mclc.serverlist.listener;

import de.scholle.mclc.serverlist.service.FaviconService;
import de.scholle.mclc.serverlist.service.PlaceholderService;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetAddress;
import java.util.Collection;

public final class ServerListPingListener implements Listener {

    private final JavaPlugin plugin;
    private final PlaceholderService placeholderService;
    private final FaviconService faviconService;

    public ServerListPingListener(JavaPlugin plugin, PlaceholderService placeholderService,
            FaviconService faviconService) {
        this.plugin = plugin;
        this.placeholderService = placeholderService;
        this.faviconService = faviconService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerListPing(ServerListPingEvent event) {
        String motd = plugin.getConfig().getString("server-list.motd", "");
        motd = motd.replace("\\n", "\n");

        OfflinePlayer playerContext = findPlayerContext(event.getAddress(), plugin.getServer().getOnlinePlayers());
        motd = placeholderService.applyPlaceholders(playerContext, motd);
        motd = ChatColor.translateAlternateColorCodes('&', motd);

        event.setMotd(motd);
        faviconService.applyIcon(event);
    }

    private OfflinePlayer findPlayerContext(InetAddress address, Collection<? extends Player> onlinePlayers) {
        if (address == null) {
            return null;
        }

        for (Player player : onlinePlayers) {
            if (player.getAddress() == null) {
                continue;
            }

            if (address.equals(player.getAddress().getAddress())) {
                return player;
            }
        }

        return null;
    }
}
