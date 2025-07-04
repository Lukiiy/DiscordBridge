package me.lukiiy.discordBridge.listeners;

import me.lukiiy.discordBridge.DiscordBridge;
import me.lukiiy.discordBridge.api.DiscordContext;
import me.lukiiy.discordBridge.api.serialize.DSerial;
import me.lukiiy.discordBridge.event.BridgeMinecraftReceiveEvent;
import me.lukiiy.discordBridge.utils.MemberHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents extends PlayerListener {
    public void onPlayerChat(PlayerChatEvent e) {
        Player p = e.getPlayer();
        BridgeMinecraftReceiveEvent bridgeEvent = new BridgeMinecraftReceiveEvent(p, e.getMessage());

        Bukkit.getServer().getPluginManager().callEvent(bridgeEvent);
        if (bridgeEvent.isCancelled()) return;

        send(DiscordBridge.getInstance().getConfiguration().getString("format.mc", e.getFormat().replace("%1$s", "(user)").replace("%2$s", "(msg)"))
                .replace("(user)", p.getDisplayName())
                .replace("(msg)", bridgeEvent.getMessage()), true);
    }

    public void onPlayerJoin(PlayerJoinEvent e) {
        send(e.getJoinMessage(), false);
    }

    public void onPlayerQuit(PlayerQuitEvent e) {
        send(e.getQuitMessage(), false);
    }

    private void send(String msg, boolean priority) {
        DiscordBridge instance = DiscordBridge.getInstance();
        DiscordContext context = instance.getContext();

        if (context == null || (priority && !instance.getConfiguration().getBoolean("discord.playerEvents", true)) || msg == null || msg.isEmpty()) return;

        context.sendMessage(DSerial.toDiscord(MemberHelper.fixMentions(msg, context.getGuild())));
    }
}
