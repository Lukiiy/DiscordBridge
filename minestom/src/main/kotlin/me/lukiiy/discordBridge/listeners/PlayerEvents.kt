package me.lukiiy.discordBridge.listeners

import me.lukiiy.discordBridge.Main
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt.toDiscord
import me.lukiiy.discordBridge.utils.MemberHelper.fixMentions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.player.PlayerDeathEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.instance.Instance

class PlayerEvents(eventHandler: GlobalEventHandler, initialInstance: Instance) {
    val plain = PlainTextComponentSerializer.plainText()

    init {
        eventHandler.apply {
            addListener(PlayerChatEvent::class.java) {
                val p = it.player

                send(Main.config.getOrDefault("messages.discord.format", "")!!
                    .replace("(user)", plain.serialize(p.name))
                    .replace("(msg)", it.rawMessage), true)
            }

            addListener(AsyncPlayerConfigurationEvent::class.java) {
                val p = it.player

                it.spawningInstance = initialInstance
                p.respawnPoint = Pos(0.0, 42.0, 0.0)
                send("${plain.serialize(p.name)} joined", false)
            }

            addListener(PlayerDisconnectEvent::class.java) { send("${plain.serialize(it.player.name)} left", false) }

            addListener(PlayerDeathEvent::class.java) { send("${plain.serialize(it.player.name)} died", false) }
        }
    }

    private fun send(msg: String, priority: Boolean) {
        if ((priority && !Main.config.getBoolean("discord.playerEvents")) || msg.isBlank()) return
        val context = Main.context

        context!!.sendMessage(toDiscord(fixMentions(msg, context.guild)))
    }
}