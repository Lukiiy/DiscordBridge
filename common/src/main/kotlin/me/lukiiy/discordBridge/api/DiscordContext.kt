package me.lukiiy.discordBridge.api

import me.lukiiy.discordBridge.utils.MemberHelper.hasRole
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.time.Duration

/**
 * Holder for common data like the bot instance, guild, channel and roles...
 */
class DiscordContext(val bot: JDA, val guild: Guild, val channel: TextChannel, val consoleAdminRole: Role?) {
    private val commands: MutableMap<String, CommandPlate> = mutableMapOf()

    companion object {
        private var inst: DiscordContext? = null

        /**
         * Get the current Discord context
         * @throws IllegalStateException if not initialized
         */
        @JvmStatic
        val instance: DiscordContext
            get() = inst ?: error("DiscordContext is not initialized!")

        /**
         * Initializes the context
         * @param bot a JDA (bot) instance
         * @param guild a Guild (server) instance
         * @param channel a TextChannel instance
         * @param consoleAdminRole a Role for the console feature
         * @return a DiscordContext instance
         */
        @JvmStatic
        fun init(bot: JDA, guild: Guild, channel: TextChannel, consoleAdminRole: Role?): DiscordContext {
            inst = DiscordContext(bot, guild, channel, consoleAdminRole)
            return inst!!
        }

        /**
         * Initializes the context using DiscordBridge's method
         * @param token your bot's token
         * @param channelId a channel ID
         * @param consoleAdminRoleId the ID for the console admin role
         * @return a DiscordContext instance
         */
        @JvmStatic
        @Throws(Exception::class, InterruptedException::class)
        fun init(token: String, channelId: Long, consoleAdminRoleId: Long): DiscordContext {
            val bot = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .disableCache(CacheFlag.CLIENT_STATUS, CacheFlag.FORUM_TAGS, CacheFlag.ONLINE_STATUS, CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.ROLE_TAGS)
                .setStatus(OnlineStatus.OFFLINE)
                .setActivity(null)
                .setMemberCachePolicy(MemberCachePolicy.VOICE.or(MemberCachePolicy.ONLINE).and(MemberCachePolicy.lru(50)))
                .build()
                .awaitReady()

            val channel = bot.getTextChannelById(channelId) ?: error("Channel with ID $channelId not found")
            val guild = channel.guild
            val consoleAdminRole = guild.getRoleById(consoleAdminRoleId)

            guild.loadMembers { channel.canTalk(it!!) }

            return init(bot, guild, channel, consoleAdminRole)
        }

        /**
         * Get a quick summary of a context.
         *
         * @param context A context
         * @return A [LinkedHashMap] of info
         */
        @JvmStatic
        fun getContextSummary(context: DiscordContext): Map<String, String> = linkedMapOf(
            "Guild" to context.guild.name,
            "Channel" to context.channel.name,
            "Connection Status" to context.bot.status.name,
            "Status" to context.bot.presence.status.name,
            "Activity" to (context.bot.presence.activity?.state ?: "Not set")
        )
    }

    /**
     * Register new commands
     * @param plates The CommandPlate(s)
     */
    fun addCommands(vararg plates: CommandPlate) {
        for (plate in plates) commands[plate.command().name] = plate
        reloadCommands()
    }

    /**
     * Reloads the registered commands
     */
    fun reloadCommands() {
        if (bot.status == JDA.Status.SHUTTING_DOWN || bot.status == JDA.Status.SHUTDOWN) return
        guild.updateCommands().addCommands(commands.values.map { it.command() }).queue()
    }

    /**
     * Unregisters every command from the bot instance (on Discord)
     */
    fun clearCommands() = guild.updateCommands().queue()

    fun getCommands(): Map<String, CommandPlate> = commands.toMap()

    fun getCommand(name: String): CommandPlate? = commands[name]

    /**
     * Stops and shutdowns the bot
     */
    @Throws(InterruptedException::class)
    fun shutdown() {
        // clearCommands()
        bot.shutdown()

        try {
            if (!bot.awaitShutdown(Duration.ofSeconds(3))) {
                bot.shutdownNow()
                bot.awaitShutdown()
            }
        } catch (e: InterruptedException) { e.printStackTrace() }
    }

    /**
     * Send a discord message to a channel
     */
    @JvmOverloads
    fun sendMessage(message: String, textChannel: TextChannel = channel) {
        if (message.isEmpty() || !textChannel.canTalk()) return
        textChannel.sendMessage(message).queue()
    }

    /**
     * Check if a member has permission to use the Minecraft server console
     */
    fun hasConsoleAdminRole(member: Member): Boolean {
        if (consoleAdminRole == null) return false

        return member.hasRole(consoleAdminRole)
    }

    /**
     * Get all members with console admin role
     */
    fun getConsoleAdmins(): List<Member> = guild.getMembersWithRoles(consoleAdminRole)

    /**
     * Get the bot's member object in the guild
     */
    fun getBotMember(): Member? = guild.getMember(bot.selfUser)
}