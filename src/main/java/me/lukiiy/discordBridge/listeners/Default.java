package me.lukiiy.discordBridge.listeners;

import me.lukiiy.discordBridge.DCBridge;
import me.lukiiy.discordBridge.util.MemberHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.BroadcastMessageEvent;

public class Default implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void chat(AsyncPlayerChatEvent e) {
        if (!DCBridge.configBool("discord.chatToMinecraft")) return;
        DCBridge.sendDCMsg(DCBridge.configString("format.mc")
                .replace("(user)", e.getPlayer().getName())
                .replace("(msg)", e.getMessage())
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void join(PlayerJoinEvent e) {send(e.getJoinMessage());}

    @EventHandler(priority = EventPriority.HIGHEST)
    public void quit(PlayerQuitEvent e) {send(e.getQuitMessage());}

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void death(PlayerDeathEvent e) {send(e.getDeathMessage());}

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void bcast(BroadcastMessageEvent e) {DCBridge.sendDCMsg(e.getMessage());}

    private void send(String msg) {
        if (!DCBridge.configBool("discord.playerEvents") || msg == null || msg.isEmpty()) return;
        DCBridge.sendDCMsg(MemberHelper.replaceMentions(msg));
    }
}
