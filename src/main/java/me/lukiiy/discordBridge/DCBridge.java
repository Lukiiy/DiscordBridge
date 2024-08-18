package me.lukiiy.discordBridge;

import lombok.Getter;
import me.lukiiy.discordBridge.api.CommandPlate;
import me.lukiiy.discordBridge.cmds.Broadcast;
import me.lukiiy.discordBridge.cmds.Reload;
import me.lukiiy.discordBridge.discordCmds.Console;
import me.lukiiy.discordBridge.listeners.Discord;
import me.lukiiy.discordBridge.listeners.Players;
import me.lukiiy.discordBridge.util.BotHelper;
import me.lukiiy.discordBridge.util.GenericHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class DCBridge extends JavaPlugin {
    @Getter private static DCBridge instance;

    private JDA bot;
    private Guild guild;
    private TextChannel channel;

    public static Role consoleAdminRole;

    private Configuration config;
    public Logger log;
    private static final Map<String, CommandPlate> commands = new HashMap<>();

    private static ConsoleCommandSender consoleCommandSender;

    @Override
    public void onEnable() {
        instance = this;
        log = getServer().getLogger();
        setupConfig();
        consoleCommandSender = new ConsoleCommandSender(getServer());

        String token = configString("discord.token");
        if (token.isEmpty()) {
            log.warning("Insert the bot token in config.yml and then restart the server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        JDALogger.setFallbackLoggerEnabled(false);
        startBot(token);

        PluginManager pm = getServer().getPluginManager();
        PlayerListener playerListener = new Players();

        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Highest, this);
        bot.addEventListener(new Discord());

        getCommand("discordbridge").setExecutor(new Reload());
        getCommand("discordbroadcast").setExecutor(new Broadcast());

        sendDCMsg(configString("msg.serverStart"));

        consoleAdminRole = guild.getRoleById(configLong("discord.consoleRoleId"));
        if (consoleAdminRole != null) addCommand(new Console());

        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(this, () -> guild.updateCommands().addCommands(commands.values().stream().map(CommandPlate::command).collect(Collectors.toList())).queue(), 20L);
    }

    @Override
    public void onDisable() {
        stopBot();
    }

    private void startBot(@NotNull String token) {
        try {
            bot = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                    .disableCache(CacheFlag.CLIENT_STATUS, CacheFlag.FORUM_TAGS, CacheFlag.ONLINE_STATUS, CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.ROLE_TAGS)
                    .setStatus(BotHelper.getStatus(configString("discord.status")))
                    .setActivity(BotHelper.getActivity(configString("discord.activity")))
                    .build().awaitReady();

            channel = bot.getTextChannelById(configLong("discord.channelId"));
            if (channel != null) guild = channel.getGuild();
        }
        catch (InterruptedException err) {log("Something has been interrupted... " + err.getMessage());}

        if (bot == null || channel == null) getServer().getPluginManager().disablePlugin(this);
    }

    public void stopBot() {
        if (bot != null) {
            sendDCMsg(configString("msg.serverStop"), instance.channel);
            guild.updateCommands().queue();
            bot.shutdown();
            try {
                if (!bot.awaitShutdown(Duration.ofSeconds(3))) {
                    bot.shutdownNow();
                    bot.awaitShutdown();
                }
            } catch (InterruptedException err) {log("Something has been interrupted... " + err.getMessage());}
            bot = null;
        }
    }

    public static JDA getJDA() {return instance.bot;}
    public static Guild getGuild() {return instance.guild;}
    public static TextChannel getChannel() {return instance.channel;}

    // Config
    public void setupConfig() {
        config = getConfiguration();
        config.load();
        config.getString("discord.token", "");
        if (config.getProperty("discord.channelId") == null) config.setProperty("discord.channelId", 1234567890123456789L);
        if (config.getProperty("discord.consoleRoleId") == null) config.setProperty("discord.consoleRoleId", 1234567890123456789L);
        config.getString("discord.activity", "playing Minecraft");
        config.getString("discord.status", "ONLINE");
        config.getBoolean("discord.chatToMinecraft", true);
        config.getBoolean("discord.playerEvents", true);
        config.getString("msg.serverStart", "**Server online!**");
        config.getString("msg.serverStop", "**Server offline!**");
        config.getString("format.dcPrefix", "§9[Discord]§r");
        config.getString("format.dc", "(user): (msg)");
        config.getString("format.mc", "<(user)> (msg)");
        config.save();
    }

    public static String configString(String path) {return instance.config.getString(path);}
    public static boolean configBool(String path) {return instance.config.getBoolean(path, true);}
    public static long configLong(String path) {return (long) instance.config.getProperty(path);}

    // Extras
    public static void sendDCMsg(@NotNull String msg) {sendDCMsg(msg, instance.channel);}
    public static void sendDCMsg(@NotNull String msg, @NotNull TextChannel c) {
        if (instance.bot == null || !c.canTalk()) return;
        c.sendMessage(GenericHelper.cleanFormat(msg)).queue();
    }

    public static void log(String info) {instance.log.info(info);}

    public static void consoleCommand(String command) {DCBridge.getInstance().getServer().dispatchCommand(consoleCommandSender, command);}

    // Addons?
    public static Map<String, CommandPlate> getCommandMap() {return commands;}
    public static void addCommand(CommandPlate cmd) {commands.put(cmd.command().getName(), cmd);}
}
