package de.scholle.mclc.bukkit.serverlist.listener;

import de.scholle.mclc.bukkit.serverlist.service.FaviconService;
import de.scholle.mclc.bukkit.serverlist.service.PlaceholderService;
import de.scholle.mclc.common.ServerListTextService;
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
import java.util.HashMap;
import java.util.Map;

public final class ServerListPingListener implements Listener {

    private final JavaPlugin plugin;
    private final PlaceholderService placeholderService;
    private final FaviconService faviconService;
    private final ServerListTextService textService;

    public ServerListPingListener(JavaPlugin plugin,
            PlaceholderService placeholderService,
            FaviconService faviconService,
            ServerListTextService textService) {
        this.plugin = plugin;
        this.placeholderService = placeholderService;
        this.faviconService = faviconService;
        this.textService = textService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerListPing(ServerListPingEvent event) {
        String motd = plugin.getConfig().getString("server-list.motd", "");
        motd = textService.normalizeMotd(motd);

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("%online%", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
        placeholders.put("%max_players%", String.valueOf(plugin.getServer().getMaxPlayers()));
        placeholders.put("%version%", plugin.getServer().getVersion());
        motd = textService.applyPlaceholders(motd, placeholders);

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
