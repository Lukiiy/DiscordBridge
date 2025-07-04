package me.lukiiy.discordBridge.cmds

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import me.lukiiy.discordBridge.DiscordBridge
import me.lukiiy.discordBridge.api.DiscordContext.Companion.getContextSummary
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object Main {
    private val subCmds = linkedMapOf(
        "reload" to "Reload the plugin",
        "broadcast" to "Broadcasts a message to Discord",
        "discordcmds" to "Show the registered commands for the bot",
        "mainconnection" to "Show some information about the main connection"
    )

    private val main = Commands.literal("discordbridge")
        .requires { it.sender.hasPermission("dcbridge.cmd") }
        .executes {
            it.source.sender.apply {
                sendMessage(Component.text("DCBridge Subcommands:").color(DSerialAdvnt.bridgeBlue))
                subCmds.forEach { (cmd, desc) -> sendMessage(Component.text("•").color(DSerialAdvnt.bridgeList).appendSpace().append(Component.text(cmd).color(NamedTextColor.GOLD)).append(Component.text(" → ")).append(Component.text(desc))) }
            }

            Command.SINGLE_SUCCESS
        }

    private val reload = Commands.literal("reload")
        .executes {
            DiscordBridge.getInstance().reloadConfig()
            it.source.sender.sendMessage(Component.text("DiscordBridge messages & settings reload complete.").color(NamedTextColor.GREEN))
            Command.SINGLE_SUCCESS
        }

    private val broadcast = Commands.literal("broadcast")
        .then(Commands.argument("message", StringArgumentType.greedyString())
            .executes {
                val msg = StringArgumentType.getString(it, "message")

                DiscordBridge.getInstance().apply {
                    context?.sendMessage(msg)
                    logger.info("[Broadcast] $msg")
                }

                it.source.sender.sendMessage(Component.text("Message broadcasted!").color(NamedTextColor.GREEN))
                Command.SINGLE_SUCCESS
            }
        )

    private val dccmds = Commands.literal("discordcmds")
        .executes {
            it.source.sender.apply {
                sendMessage(Component.text("Available Discord commands:").color(DSerialAdvnt.bridgeBlue))
                DiscordBridge.getInstance().context?.getCommands()?.keys?.forEach { cmd -> sendMessage(Component.text("•").color(DSerialAdvnt.bridgeList).appendSpace().append(Component.text(cmd).color(NamedTextColor.GOLD))) }
            }
            Command.SINGLE_SUCCESS
        }

    private val mainConnection = Commands.literal("mainconnection")
        .executes {
            val ctx = DiscordBridge.getInstance().context ?: throw SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.text("No main context found.").color(NamedTextColor.RED))).create()

            it.source.sender.apply {
                sendMessage(Component.text("Main connection info:").color(DSerialAdvnt.bridgeBlue))
                getContextSummary(ctx).forEach { (i, v) -> sendMessage(Component.text("•").color(DSerialAdvnt.bridgeList).appendSpace().append(Component.text("$i: ").color(NamedTextColor.YELLOW)).append(Component.text(v))) }
            }
            Command.SINGLE_SUCCESS
        }

    fun register(): LiteralCommandNode<CommandSourceStack> = main.then(reload).then(broadcast).then(dccmds).then(mainConnection).build()
}