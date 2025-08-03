package me.lukiiy.discordBridge

import me.clip.placeholderapi.PlaceholderAPI
import me.lukiiy.discordBridge.api.DiscordContext
import me.lukiiy.discordBridge.api.DiscordContext.Companion.init
import me.lukiiy.discordBridge.discordCmds.Console
import me.lukiiy.discordBridge.listeners.DefaultEvents
import me.lukiiy.discordBridge.listeners.DiscordEvents
import me.lukiiy.discordBridge.utils.BotHelper
import me.lukiiy.discordBridge.utils.MemberHelper.getHexColor
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Member
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.time.Duration

class DiscordBridge : JavaPlugin() {
    var context: DiscordContext? = null
        private set

    private var placeholderAPIHook = false

    override fun onEnable() {
        setupConfig()

        server.pluginManager.registerEvents(DefaultEvents(), this)

        // Bot
        val token = config.getString("discord.token")
        if (token == null || token.isBlank()) {
            logger.warning("Insert the bot token in config.yml and then restart the server.")
            server.pluginManager.disablePlugin(this)
            return
        }

        if (isFolia()) server.globalRegionScheduler.runDelayed(this, { initBot(token) }, 5L)
        else server.asyncScheduler.runNow(this) { initBot(token) }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) placeholderAPIHook = true
    }

    override fun onDisable() {
        context?.apply {
            if (config.getBoolean("discord.clearCommandsWhenShutdown")) clearCommands()

            sendMessage(config.getString("messages.discord.stop", "")!!)
            shutdown(Duration.ofSeconds(config.getLong("discord.shutdownLimit", 3)), if (isFolia()) 500 else 2000)
        }

        context = null
    }

    companion object {
        fun getInstance(): DiscordBridge = getPlugin(DiscordBridge::class.java)
    }

    fun miniSerializableName(member: Member): String = if (config.getBoolean("discord.useMemberNameColor")) "<color:" + member.getHexColor() + ">" + member.effectiveName + "</color>" else member.effectiveName

    private fun initBot(token: String) {
        try {
            context = init(token, config.getLong("discord.channelId"), config.getLong("discord.consoleRoleId")).apply {
                bot.presence.setPresence(OnlineStatus.fromKey(config.getString("discord.status", "")!!), BotHelper.getActivity(config.getString("discord.activity", "")!!))
                sendMessage(config.getString("messages.discord.start", "")!!)
                bot.addEventListener(DiscordEvents())

                if (consoleAdminRole != null) addCommands(Console())
            }
        } catch (e: Exception) {
            logger.severe(e.message)

            if (isFolia()) server.globalRegionScheduler.run(this) { server.pluginManager.disablePlugin(this) }
            else server.asyncScheduler.runNow(this) { server.pluginManager.disablePlugin(this) }
        }
    }


    // Config
    fun setupConfig() {
        saveDefaultConfig()
        config.options().copyDefaults(true)
        saveConfig()
    }

    // Hooks
    fun parsePlaceholders(player: Player?, message: String?): String? {
        if (!placeholderAPIHook || player == null || message == null) return message

        return PlaceholderAPI.setPlaceholders(player, message)
    }

    private fun isFolia(): Boolean = try {
        Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}
