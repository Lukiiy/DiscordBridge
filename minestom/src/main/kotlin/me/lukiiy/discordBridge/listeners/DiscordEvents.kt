package me.lukiiy.discordBridge.listeners

import me.lukiiy.discordBridge.Main
import me.lukiiy.discordBridge.api.MessageParts
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt.MINI
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt.fromDiscord
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.collections.forEach

class DiscordEvents : ListenerAdapter() {
    override fun onMessageReceived(e: MessageReceivedEvent) {
        val context = Main.context
        val config = Main.config

        if (e.channel !== context!!.channel) return

        val member = e.member
        val msg = e.message

        val parts = MessageParts.from(
            config.getOrDefault("messages.minecraft.format", "")!!.replace("(user)", Main.miniSerializableName(member!!)),
            config.getOrDefault("messages.minecraft.prefix", "")!!,
            member,
            e.message,
            context.bot,
            config.getBoolean("discord.ignoreBots"),
            !config.getOrDefault("messages.minecraft.reply.default", "")!!.isBlank(),
            config.getOrDefault("messages.minecraft.reply.default", ""),
            config.getBoolean("messages.minecraft.reply.ignoreBot")
        ) ?: return

        val prefix = if (parts.prefix.isBlank()) Component.empty() else MINI.deserialize(parts.prefix)
        val content = fromDiscord(parts.content)
        val formatted = Component.empty().append(prefix).append(content).appendSpace().append(Component.join(JoinConfiguration.spaces(), DSerialAdvnt.listAttachments(msg)))

        MinecraftServer.getConnectionManager().onlinePlayers.forEach { it.sendMessage(formatted) }
        println(PlainTextComponentSerializer.plainText().serialize(formatted))
    }

    override fun onGenericCommandInteraction(e: GenericCommandInteractionEvent) {
        val cmd = Main.context!!.getCommand(e.name) ?: return

        cmd.interaction(e)
        println(PlainTextComponentSerializer.plainText().serialize(MINI.deserialize("[Discord] " + Main.miniSerializableName(e.member!!) + " issued server command: /" + e.fullCommandName)))
    }

    override fun onThreadMemberJoin(e: ThreadMemberJoinEvent) {
        val thread = e.getThread()
        if (!thread.isPublic || thread.isArchived) return

        if (Duration.between(thread.timeCreated, OffsetDateTime.now()).toMinutes() < 1) {
            val msg = MINI.deserialize(Main.config.get("messages.minecraft.threadCreation")?.replace("(name)", thread.name) ?: "")
            if (msg == Component.empty()) return

            MinecraftServer.getConnectionManager().onlinePlayers.forEach { it.sendMessage(msg) }
            println(PlainTextComponentSerializer.plainText().serialize(msg))
        }
    }
}
