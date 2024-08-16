package me.lukiiy.discordBridge;

import lombok.Getter;
import me.lukiiy.discordBridge.api.CommandPlate;
import me.lukiiy.discordBridge.cmds.Broadcast;
import me.lukiiy.discordBridge.cmds.Reload;
import me.lukiiy.discordBridge.discordCmds.Console;
import me.lukiiy.discordBridge.listeners.Default;
import me.lukiiy.discordBridge.listeners.Discord;
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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class DCBridge extends JavaPlugin {
    @Getter private static DCBridge instance;
    
    private JDA bot;
    private Guild guild;
    private TextChannel channel;

    public static Role consoleAdminRole;

    private FileConfiguration config;
    private static Audience console;
    private static final Map<String, CommandPlate> commands = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        GenericHelper.audience = BukkitAudiences.create(this);
        setupConfig();

        String token = configString("discord.token");
        if (token.isEmpty()) {
            getLogger().warning("Insert the bot token in config.yml and then restart the server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        console = GenericHelper.audience.console();

        JDALogger.setFallbackLoggerEnabled(false);
        startBot(token);

        getServer().getPluginManager().registerEvents(new Default(), this);
        bot.addEventListener(new Discord());

        getCommand("discordbridge").setExecutor(new Reload());
        getCommand("discordbroadcast").setExecutor(new Broadcast());

        sendDCMsg(configString("msg.serverStart"));

        consoleAdminRole = guild.getRoleById(configLong("discord.consoleRoleId"));
        if (consoleAdminRole != null) addCommand(new Console());

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> guild.updateCommands().addCommands(commands.values().stream().map(CommandPlate::command).toList()).queue(), 20L);
    }

    @Override
    public void onDisable() {
        stopBot();
        if (GenericHelper.audience != null) GenericHelper.audience.close();
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
        saveDefaultConfig();
        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();
    }

    public static String configString(String path) {return instance.config.getString(path);}
    public static boolean configBool(String path) {return instance.config.getBoolean(path, true);}
    public static long configLong(String path) {return instance.config.getLong(path);}

    // Extras
    public static void sendDCMsg(@NotNull String msg) {sendDCMsg(msg, instance.channel);}
    public static void sendDCMsg(@NotNull String msg, @NotNull TextChannel c) {
        if (instance.bot == null || !c.canTalk()) return;
        c.sendMessage(GenericHelper.cleanFormat(msg)).queue();
    }

    public static void log(String info) {log(Component.text(info));}
    public static void log(Component info) {console.sendMessage(info);}

    // Addons?
    public static Map<String, CommandPlate> getCommandMap() {return commands;}
    public static void addCommand(CommandPlate cmd) {commands.put(cmd.command().getName(), cmd);}
}
