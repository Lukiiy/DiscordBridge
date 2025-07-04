package me.lukiiy.discordBridge.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import me.lukiiy.discordBridge.DiscordBridge.Companion.getInstance
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt.toDiscord
import me.lukiiy.discordBridge.event.BridgeMinecraftReceiveEvent
import me.lukiiy.discordBridge.utils.MemberHelper.fixMentions
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.BroadcastMessageEvent


class DefaultEvents : Listener {
    private val plain = PlainTextComponentSerializer.plainText()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun chat(e: AsyncChatEvent) {
        val p = e.getPlayer()
        val bridgeEvent = BridgeMinecraftReceiveEvent(p, e.message())

        getInstance().server.globalRegionScheduler.run(getInstance()) { task ->
            Bukkit.getPluginManager().callEvent(bridgeEvent)
            if (bridgeEvent.isCancelled()) return@run

            send(p, getInstance().getConfig().getString("messages.discord.format", "")!!
                    .replace("(user)", plain.serialize(p.displayName()))
                    .replace("(msg)", plain.serialize(bridgeEvent.message)), true)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun join(e: PlayerJoinEvent) = send(e.getPlayer(), plain.serialize(e.joinMessage()!!), false)

    @EventHandler(priority = EventPriority.HIGHEST)
    fun quit(e: PlayerQuitEvent) = send(e.getPlayer(), plain.serialize(e.quitMessage()!!), false)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun death(e: PlayerDeathEvent) = send(e.getEntity(), plain.serialize(e.deathMessage()!!), false)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun bcast(e: BroadcastMessageEvent) = send(null, plain.serialize(e.message()), false)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun advancement(e: PlayerAdvancementDoneEvent) {
        val p = e.player

        val display = e.advancement.display ?: run { return }
        if (e.message() == null || !display.doesAnnounceToChat() || p.world.getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS) == false || Bukkit.getWorlds().first().getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS) == false) return

        send(p, plain.serialize(e.message()!!), false)
    }

    private fun send(player: Player?, msg: String, priority: Boolean) {
        var msg = msg
        val instance = getInstance()
        val context = instance.context

        if ((priority && !instance.getConfig().getBoolean("discord.playerEvents")) || msg.isBlank()) return
        msg = instance.parsePlaceholders(player, msg)!!

        context!!.sendMessage(toDiscord(fixMentions(msg, context.guild)))
    }
}
