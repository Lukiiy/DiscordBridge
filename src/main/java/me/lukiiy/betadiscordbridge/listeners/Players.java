package me.lukiiy.betadiscordbridge.listeners;

import me.lukiiy.betadiscordbridge.DiscordBridge;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class Players extends PlayerListener {
    DiscordBridge bridge = DiscordBridge.inst;

    public void onPlayerJoin(PlayerJoinEvent e) {bridge.sendDCMsg(e.getJoinMessage());}
    public void onPlayerQuit(PlayerQuitEvent e) {bridge.sendDCMsg(e.getQuitMessage());}

    public void onPlayerChat(PlayerChatEvent e) {
        String formatted = e.getFormat()
                .replace("%1$s", e.getPlayer().getDisplayName())
                .replace("%2$s", e.getMessage());
        bridge.sendDCMsg(formatted);
    }
}
