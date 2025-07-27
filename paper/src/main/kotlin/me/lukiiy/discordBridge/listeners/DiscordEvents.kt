package me.lukiiy.discordBridge.listeners

import me.lukiiy.discordBridge.DiscordBridge.Companion.getInstance
import me.lukiiy.discordBridge.api.DiscordContext
import me.lukiiy.discordBridge.api.MessageParts
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt.MINI
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt.fromDiscord
import me.lukiiy.discordBridge.event.BridgeDiscordReceiveEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent
import net.dv8tion.jda.api.events.session.SessionResumeEvent
import net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicReference

class DiscordEvents : ListenerAdapter() {
    override fun onMessageReceived(e: MessageReceivedEvent) {
        val instance = getInstance()
        val context = AtomicReference<DiscordContext?>(instance.context)
        val config = instance.getConfig()

        if (e.getChannel() !== context.get()!!.channel) return

        val member = e.member
        val msg = AtomicReference(e.message)

        instance.server.globalRegionScheduler.run(instance) {
            val event = BridgeDiscordReceiveEvent(context.get()!!, member, msg.get()!!)
            Bukkit.getPluginManager().callEvent(event)

            if (event.isCancelled()) {
                it!!.cancel()
                return@run
            }

            context.set(event.context)
            msg.set(event.message)

            val parts = MessageParts.from(
                config.getString("messages.minecraft.format", "")!!.replace("(user)", instance.miniSerializableName(member!!)),
                config.getString("messages.minecraft.prefix", "")!!,
                member,
                msg.get()!!,
                context.get()!!.bot,
                config.getBoolean("discord.ignoreBots"),
                !config.getString("messages.minecraft.reply.default", "")!!.isBlank(),
                config.getString("messages.minecraft.reply.default", ""),
                config.getBoolean("messages.minecraft.reply.ignoreBot")
            )

            if (parts == null) {
                it!!.cancel()
                return@run
            }

            val prefix = if (parts.prefix.isBlank()) Component.empty() else MINI.deserialize(parts.prefix)
            val content = fromDiscord(parts.content)
            val formatted: Component = Component.empty().append(prefix).append(content).appendSpace().append(Component.join(JoinConfiguration.spaces(), DSerialAdvnt.listAttachments(msg.get()!!)))

            instance.server.onlinePlayers.forEach { p: Player? -> p!!.sendMessage(formatted) }
            instance.componentLogger.info(formatted)
        }
    }

    override fun onGenericCommandInteraction(e: GenericCommandInteractionEvent) {
        val instance = getInstance()
        val cmd = instance.context!!.getCommand(e.name) ?: return

        cmd.interaction(e)
        instance.logger.info(PlainTextComponentSerializer.plainText().serialize(MINI.deserialize("[Discord] " + instance.miniSerializableName(e.member!!) + " issued server command: /" + e.getFullCommandName())))
    }

    override fun onThreadMemberJoin(e: ThreadMemberJoinEvent) {
        val thread = e.getThread()
        if (!thread.isPublic || thread.isArchived) return

        val instance = getInstance()

        if (Duration.between(thread.timeCreated, OffsetDateTime.now()).toMinutes() < 1) {
            val msg = MINI.deserialize(instance.getConfig().getString("messages.minecraft.threadCreation")?.replace("(name)", thread.name) ?: "")
            if (msg == Component.empty()) return

            instance.server.onlinePlayers.forEach { p: Player? -> p!!.sendMessage(msg) }
            instance.componentLogger.info(msg)
        }
    }
}
