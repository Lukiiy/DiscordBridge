package me.lukiiy.discordBridge;

import me.lukiiy.discordBridge.api.DiscordContext;
import me.lukiiy.discordBridge.cmds.Main;
import me.lukiiy.discordBridge.discordCmds.Console;
import me.lukiiy.discordBridge.listeners.DiscordEvents;
import me.lukiiy.discordBridge.listeners.PlayerEvents;
import me.lukiiy.discordBridge.utils.BotHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.config.Configuration;

import java.time.Duration;
import java.util.logging.Logger;

public final class DiscordBridge extends JavaPlugin {
    private static DiscordBridge instance;
    private DiscordContext context;
    private Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        setupConfig();

        PluginManager pluginManager = getServer().getPluginManager();
        Configuration config = getConfiguration();
        logger = getServer().getLogger();

        PlayerEvents pListener = new PlayerEvents();
        pluginManager.registerEvent(Event.Type.PLAYER_JOIN, pListener, Event.Priority.Monitor, this);
        pluginManager.registerEvent(Event.Type.PLAYER_QUIT, pListener, Event.Priority.Monitor, this);
        pluginManager.registerEvent(Event.Type.PLAYER_CHAT, pListener, Event.Priority.Monitor, this);

        getCommand("discordbridge").setExecutor(new Main());

        // Bot
        String token = config.getString("discord.token");
        if (token == null || token.isEmpty()) {
            logger.warning("Insert the bot token in config.yml and then restart the server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        BukkitScheduler scheduler = getServer().getScheduler();

        final long channelId = str2long(config.getString("discord.channelId"));
        final long consoleAccessRole = str2long(config.getString("discord.consoleRoleId"));
        final String activityKey = config.getString("discord.activity", "");
        final String statusKey = config.getString("discord.status", "");
        final String startMsg = config.getString("messages.discord.start", "");

        scheduler.scheduleAsyncDelayedTask(this, () -> {
            try {
                context = DiscordContext.init(token, channelId, consoleAccessRole);
                final JDA bot = context.getBot();

                Activity activity = null;
                try {
                    activity = BotHelper.getActivity(activityKey);
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }

                bot.getPresence().setPresence(OnlineStatus.fromKey(statusKey), activity);
                context.sendMessage(startMsg);

                if (context.getConsoleAdminRole() != null) context.addCommands(new Console(getServer()));
                bot.addEventListener(new DiscordEvents());
            } catch (Exception e) {
                logger.warning(e.getMessage());
                scheduler.scheduleSyncDelayedTask(this, () -> getServer().getPluginManager().disablePlugin(this));
            }
        });
    }

    @Override
    public void onDisable() {
        if (context != null) {
            if (getConfiguration().getBoolean("discord.shutdown.clearCommands", true)) context.clearCommands();

            context.sendMessage(getConfiguration().getString("messages.discord.stop", ""));
            context.shutdown(Duration.ofSeconds(str2long(getConfiguration().getString("discord.shutdown.timeLimit", "3"))));

            context = null;
        }
    }

    public static DiscordBridge getInstance() {
        return instance;
    }

    public DiscordContext getContext() {
        return context;
    }

    // Config
    public void setupConfig() {
        Configuration config = getConfiguration();
        
        config.load();

        config.getString("discord.token", "");
        if (config.getProperty("discord.channelId") == null) config.setProperty("discord.channelId", 1234567890123456789L);
        if (config.getProperty("discord.consoleRoleId") == null) config.setProperty("discord.consoleRoleId", 1234567890123456789L);
        config.getString("discord.activity", "playing Minecraft");
        config.getString("discord.status", "ONLINE");
        config.getBoolean("discord.playerEvents", true);
        config.getBoolean("discord.ignoreBots", false);
        if (config.getProperty("discord.shutdown.timeLimit") == null) config.setProperty("discord.shutdown.timeLimit", 3);
        config.getBoolean("discord.shutdown.clearCommands", true);

        config.getString("messages.discord.start", "**Server online!**");
        config.getString("messages.discord.stop", "**Server offline!**");
        config.getString("messages.discord.format", "<(user)> (msg)");

        config.getString("messages.minecraft.prefix", "§9[Discord]§f");
        config.getString("messages.minecraft.format", "(user):(reply) (msg)");
        config.getString("messages.minecraft.threadCreation", "§dA new thread \"(name)\" has been created!");
        config.getString("messages.minecraft.userJoin", "§e(user) has joined the discord server!");
        config.getString("messages.minecraft.reply.default", "\\ (user)");
        config.getBoolean("messages.minecraft.reply.ignoreBot", true);

        config.save();
    }

    private long str2long(String string) {
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public void commonSend(String msg) {
        getServer().broadcastMessage(msg);
        getServer().getLogger().info(msg);
    }

    // Java backports
    public static boolean isBlank(String input) {
        return input == null || input.isEmpty() || input.chars().allMatch(Character::isWhitespace);
    }
}
