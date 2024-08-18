package me.lukiiy.discordBridge.listeners;

import me.lukiiy.discordBridge.DCBridge;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class Players extends PlayerListener {

    public void chat(PlayerChatEvent e) {
        DCBridge.sendDCMsg(DCBridge.configString("format.mc")
                .replace("(user)", e.getPlayer().getName())
                .replace("(msg)", e.getMessage())
        );
    }

    public void onPlayerJoin(PlayerJoinEvent e) {DCBridge.sendDCMsg(e.getJoinMessage());}
    public void onPlayerQuit(PlayerQuitEvent e) {DCBridge.sendDCMsg(e.getQuitMessage());}

    public void onPlayerChat(PlayerChatEvent e) {
        String formatted = e.getFormat()
                .replace("%1$s", e.getPlayer().getDisplayName())
                .replace("%2$s", e.getMessage());
        DCBridge.sendDCMsg(formatted);
    }
}
