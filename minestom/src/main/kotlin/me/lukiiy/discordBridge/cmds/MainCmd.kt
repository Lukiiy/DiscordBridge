package me.lukiiy.discordBridge.cmds

import me.lukiiy.discordBridge.Main
import me.lukiiy.discordBridge.api.DiscordContext.Companion.getContextSummary
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType

object MainCmd : Command("discordbridge", "dcbridge", "dcb") {
    private val subCmds = linkedMapOf(
        "broadcast" to "Broadcasts a message to Discord",
        "discordcmds" to "Show the registered commands for the bot",
        "mainconnection" to "Show some information about the main connection"
    )

    init {
        setCondition { sender, _ -> sender == MinecraftServer.getCommandManager().consoleSender }

        setDefaultExecutor { sender, _ ->
            sender.sendMessage(Component.text("DCBridge Subcommands:").color(DSerialAdvnt.bridgeBlue))

            subCmds.forEach { (cmd, desc) ->
                sender.sendMessage(Component.text("•").color(DSerialAdvnt.bridgeList).appendSpace().append(Component.text(cmd).color(NamedTextColor.GOLD)).append(Component.text(" → ")).append(Component.text(desc)))
            }
        }

        val msgArgument = ArgumentType.StringArray("message")

        addSyntax({ sender, syntaxCtx ->
            val msg = syntaxCtx.get(msgArgument).joinToString(" ")

            Main.apply {
                context?.sendMessage(msg)
                MinecraftServer.LOGGER.info("[Broadcast] $msg")
            }

            sender.sendMessage(Component.text("Message broadcasted!").color(NamedTextColor.GREEN))
        }, ArgumentType.Literal("broadcast"), msgArgument)

        addSyntax({ sender, _ ->
            sender.sendMessage(Component.text("Available Discord commands:").color(DSerialAdvnt.bridgeBlue))

            Main.context?.getCommands()?.keys?.forEach { sender.sendMessage(Component.text("•").color(DSerialAdvnt.bridgeList).appendSpace().append(Component.text(it).color(NamedTextColor.GOLD))) }
        }, ArgumentType.Literal("discordcmds"))

        addSyntax({ sender, _ ->
            if (Main.context == null) {
                sender.sendMessage(Component.text("No main context found.").color(NamedTextColor.RED))
                return@addSyntax
            }

            sender.sendMessage(Component.text("Main connection info:").color(DSerialAdvnt.bridgeBlue))

            getContextSummary(Main.context!!).forEach { (i, v) ->
                sender.sendMessage(Component.text("•").color(DSerialAdvnt.bridgeList).appendSpace().append(Component.text("$i: ").color(NamedTextColor.YELLOW)).append(Component.text(v)))
            }
        }, ArgumentType.Literal("mainconnection"))
    }
}