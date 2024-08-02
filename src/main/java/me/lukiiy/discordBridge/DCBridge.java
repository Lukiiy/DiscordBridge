package me.lukiiy.discordBridge;

import me.lukiiy.discordBridge.api.CommandManager;
import me.lukiiy.discordBridge.cmds.Broadcast;
import me.lukiiy.discordBridge.cmds.Reload;
import me.lukiiy.discordBridge.dccmds.Console;
import me.lukiiy.discordBridge.listeners.Default;
import me.lukiiy.discordBridge.listeners.Discord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DCBridge extends JavaPlugin {
    private static DCBridge inst;
    private JDA bot;
    private Guild guild;
    private TextChannel channel;

    public static Role consoleAdminRole;

    public static BukkitAudiences bukkitAud;
    public static Audience console;
    public static final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        inst = this;
        bukkitAud = BukkitAudiences.create(this);
        setupConfig();

        String token = configString("discord.token");
        if (token.isEmpty()) {
            getLogger().warning("Insert the bot token in config.yml and then restart the server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        startBot(token);

        console = bukkitAud.console();
        CommandManager.add(new Console());

        getServer().getPluginManager().registerEvents(new Default(), this);
        bot.addEventListener(new Discord());

        getCommand("discordbridge").setExecutor(new Reload());
        getCommand("discordbroadcast").setExecutor(new Broadcast());

        sendDCMsg(configString("msg.serverStart"));

        Bukkit.getScheduler().runTaskLater(this, () -> CommandManager.load(guild.updateCommands()), 20L);
    }

    @Override
    public void onDisable() {
        sendDCMsg(configString("msg.serverStop"), inst.channel);
        guild.updateCommands().queue();
        if (bukkitAud != null) bukkitAud.close();
        stopBot();
    }

    public static DCBridge getInstance() {return inst;}
    public static JDA getJDA() {return inst.bot;}
    public static Guild getGuild() {return inst.guild;}
    public static TextChannel getChannel() {return inst.channel;}

    // Config
    public String configString(String path) {return getConfig().getString(path);}
    public boolean configBool(String path) {return getConfig().getBoolean(path, true);}
    public long configLong(String path) {return getConfig().getLong(path);}

    public void setupConfig() {
        saveDefaultConfig();
        getConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    // Extras
    public static void sendDCMsg(@NotNull String msg) {sendDCMsg(msg, inst.channel);}
    public static void sendDCMsg(@NotNull String msg, @NotNull TextChannel c) {
        if (!c.canTalk()) return;
        c.sendMessage(Util.cleanFormat(msg)).queue();
    }

    public static void log(String info) {log(Component.text(info));}
    public static void log(Component info) {bukkitAud.console().sendMessage(info);}

    private void startBot(@NotNull String token) {
        try {
            bot = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                    .disableCache(CacheFlag.CLIENT_STATUS, CacheFlag.FORUM_TAGS, CacheFlag.ONLINE_STATUS, CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.ROLE_TAGS)
                    .build().awaitReady();
            
            channel = bot.getTextChannelById(configLong("discord.channelId"));
            if (channel != null) {
                guild = channel.getGuild();
                consoleAdminRole = guild.getRoleById(configLong("discord.consoleAdminRoleId"));
            }
        }
        catch (Throwable err) {err.printStackTrace();}

        if (bot == null || channel == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupPresence(configString("discord.status"), configString("discord.activity"));
    }
    
    private void stopBot() {
        if (bot == null) return;
        bot.shutdownNow();
    }
    
    private void setupPresence(@NotNull String status, @NotNull String activity) {
        OnlineStatus status1 = OnlineStatus.ONLINE;
        try {status1 = OnlineStatus.valueOf(status);}
        catch (Throwable ignored) {}

        bot.getPresence().setStatus(status1);
        if (activity.isEmpty()) return;

        String[] part = activity.split("\\s+", 2);
        String what = part.length > 1 ? part[1] : activity;

        Activity.ActivityType type = Activity.ActivityType.PLAYING;
        try {type = Activity.ActivityType.valueOf(part[0].toUpperCase());}
        catch (Throwable ignored) {}

        bot.getPresence().setActivity(Activity.of(type, what));
    }
}
