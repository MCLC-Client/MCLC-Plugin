package de.scholle.mclc.bungee.serverlist;

import de.scholle.mclc.common.IconFileService;
import de.scholle.mclc.common.ServerListTextService;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import de.scholle.mclc.bungee.MCLCBungeePlugin;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class ServerListPingListener implements Listener {

    private final MCLCBungeePlugin plugin;
    private final ServerListTextService textService;
    private final IconFileService iconFileService;

    public ServerListPingListener(MCLCBungeePlugin plugin) {
        this.plugin = plugin;
        this.textService = new ServerListTextService();
        this.iconFileService = new IconFileService();
    }

    @EventHandler
    public void onProxyPing(ProxyPingEvent event) {
        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(new File(plugin.getDataFolder(), "config.yml"));

            String motd = config.getString("server-list.motd", "");
            motd = textService.normalizeMotd(motd);

            ServerPing response = event.getResponse();
            Map<String, String> placeholders = new HashMap<String, String>();
            placeholders.put("%online%", String.valueOf(ProxyServer.getInstance().getOnlineCount()));
            placeholders.put("%max_players%", String.valueOf(response.getPlayers().getMax()));
            placeholders.put("%version%", response.getVersion().getName());
            motd = textService.applyPlaceholders(motd, placeholders);
            motd = ChatColor.translateAlternateColorCodes('&', motd);

            response.setDescriptionComponent(new TextComponent(motd));

            boolean iconEnabled = config.getBoolean("server-list.favicon.enabled", true);
            if (iconEnabled) {
                String iconPath = config.getString("server-list.favicon.path", "server-icon.png");
                File iconFile = iconFileService.resolveIconFile(plugin.getDataFolder(), iconPath);
                if (iconFile.exists() && iconFile.isFile()) {
                    File icon64 = iconFileService.ensure64x64Png(iconFile, plugin.getDataFolder(),
                            "server-icon-resized.png");
                    response.setFavicon(Favicon.create(ImageIO.read(icon64)));
                }
            }
        } catch (Exception exception) {
            plugin.getLogger().warning("Serverlist-Ping konnte nicht verarbeitet werden: " + exception.getMessage());
        }
    }
}
