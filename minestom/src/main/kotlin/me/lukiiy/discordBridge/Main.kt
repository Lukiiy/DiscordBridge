package me.lukiiy.discordBridge

import me.lukiiy.discordBridge.api.DiscordContext
import me.lukiiy.discordBridge.cmds.MainCmd
import me.lukiiy.discordBridge.discordCmds.Console
import me.lukiiy.discordBridge.listeners.DiscordEvents
import me.lukiiy.discordBridge.listeners.PlayerEvents
import me.lukiiy.discordBridge.utils.BotHelper
import me.lukiiy.discordBridge.utils.MemberHelper.getHexColor
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Member
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.InstanceManager
import net.minestom.server.instance.block.Block
import java.time.Duration

object Main {
    var context: DiscordContext? = null
        private set

    val config = Config("config.properties", "DiscordBridge")

    @JvmStatic
    fun main(args: Array<String>) {
        config.apply {
            setIfAbsent("discord.token", "")
            setIfAbsent("discord.channelId", "1234567890123456789")
            setIfAbsent("discord.consoleRoleId", "1234567890123456789")
            setIfAbsent("discord.activity", "playing Minecraft")
            setIfAbsent("discord.status", "ONLINE")
            setIfAbsent("discord.playerEvents", "true")
            setIfAbsent("discord.useMemberNameColor", "true")
            setIfAbsent("discord.ignoreBots", "false")

            setIfAbsent("messages.discord.start", "**Server online!**")
            setIfAbsent("messages.discord.stop", "**Server offline!**")
            setIfAbsent("messages.discord.format", "<(user)> (msg)")

            setIfAbsent("messages.minecraft.prefix", "<hover:show_text:'<blue>User: (userid)<newline>Message ID: (id)</blue>'><click:suggest_command:(id)><c:#647ff8>[Discord]</c></click></hover>")
            setIfAbsent("messages.minecraft.format", "(user):(reply) (msg)")
            setIfAbsent("messages.minecraft.threadCreation", "<c:#cf6eff>A new thread \"(name)\" has been created!</c>")
            setIfAbsent("messages.minecraft.userJoin", "<yellow>(user) has joined the discord server!</yellow>")
            setIfAbsent("messages.minecraft.reply.default", "<i><c:#7175a3>└ (user)</c></i>")
            setIfAbsent("messages.minecraft.reply.ignoreBot", "true")
        }

        val token = config.get("discord.token")
        if (token.isNullOrBlank()) {
            println("Insert the bot token in config.properties and restart the server.")
            return
        }

        try {
            context = DiscordContext.init(token, config.getLong("discord.channelId"), config.getLong("discord.consoleRoleId")).apply {
                bot.presence.setPresence(OnlineStatus.fromKey(config.getOrDefault("discord.status", "")!!), BotHelper.getActivity(config.getOrDefault("discord.activity", "")!!))
                sendMessage(config.getOrDefault("messages.discord.start", "")!!)
                bot.addEventListener(DiscordEvents())

                if (consoleAdminRole != null) addCommands(Console())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            context?.apply {
                if (config.getBoolean("discord.shutdown.clearCommands")) clearCommands()

                sendMessage(config.getOrDefault("messages.discord.stop", "")!!)
                shutdown(Duration.ofSeconds(config.getLong("discord.shutdown.timeLimit")))
            }

            context = null
        })

        val server = MinecraftServer.init()
        val instanceManager: InstanceManager = MinecraftServer.getInstanceManager()
        val instanceContainer: InstanceContainer = instanceManager.createInstanceContainer()

        instanceContainer.setGenerator { it.modifier().fillHeight(0, 40, Block.STONE) }
        PlayerEvents(MinecraftServer.getGlobalEventHandler(), instanceContainer)
        MinecraftServer.getCommandManager().register(MainCmd)

        server.start("0.0.0.0", 25565)
    }

    fun miniSerializableName(member: Member): String = if (config.getBoolean("discord.useMemberNameColor")) "<color:" + member.getHexColor() + ">" + member.effectiveName + "</color>" else member.effectiveName
}