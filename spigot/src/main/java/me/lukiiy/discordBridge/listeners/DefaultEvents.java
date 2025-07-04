package me.lukiiy.discordBridge.listeners;

import me.lukiiy.discordBridge.DiscordBridge;
import me.lukiiy.discordBridge.api.DiscordContext;
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt;
import me.lukiiy.discordBridge.event.BridgeMinecraftReceiveEvent;
import me.lukiiy.discordBridge.utils.MemberHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.BroadcastMessageEvent;

public class DefaultEvents implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void chat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        DiscordBridge instance = DiscordBridge.getInstance();
        BridgeMinecraftReceiveEvent bridgeEvent = new BridgeMinecraftReceiveEvent(p, e.getMessage());

        instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> {
            Bukkit.getPluginManager().callEvent(bridgeEvent);
            if (bridgeEvent.isCancelled()) return;

            send(p, DiscordBridge.getInstance().getConfig().getString("messages.discord.format", "")
                    .replace("(user)", p.getDisplayName())
                    .replace("(msg)", bridgeEvent.getMessage()), true);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void join(PlayerJoinEvent e) {
        send(e.getPlayer(), e.getJoinMessage(), false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void quit(PlayerQuitEvent e) {
        send(e.getPlayer(), e.getQuitMessage(), false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void death(PlayerDeathEvent e) {
        send(e.getEntity(), e.getDeathMessage(), false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void bcast(BroadcastMessageEvent e) {
        send(null, e.getMessage(), false);
    }

    private void send(Player player, String msg, boolean priority) {
        DiscordBridge instance = DiscordBridge.getInstance();
        DiscordContext context = instance.getContext();

        if ((priority && !instance.getConfig().getBoolean("discord.playerEvents")) || DiscordBridge.isBlank(msg)) return;
        msg = instance.parsePlaceholders(player, msg);

        context.sendMessage(DSerialAdvnt.toDiscord(MemberHelper.fixMentions(msg, context.getGuild())));
    }
}
