package me.lukiiy.betadiscordbridge;

import me.lukiiy.betadiscordbridge.cmds.Reload;
import me.lukiiy.betadiscordbridge.listeners.Discord;
import me.lukiiy.betadiscordbridge.listeners.Players;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class DiscordBridge extends JavaPlugin {
    public static DiscordBridge inst;
    public final Logger logger = Logger.getLogger("Minecraft");
    public static final String prefix = "[DCBridge] ";
    public JDA bot;
    public Guild guild;
    public TextChannel channel;

    @Override
    public void onEnable() {
        inst = this;
        configSetup();

        String token = configString("token");
        if (token.isEmpty()) {
            logger.warning(prefix + "Insert the bot token in config.yml and then restart the server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // JDA & bot init
        JDALogger.setFallbackLoggerEnabled(false);
        try {
            bot = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS)
                    .setActivity(Activity.playing("Minecraft Beta 1.7.3"))
                    .addEventListeners(new Discord())
                    .build().awaitReady();

            channel = bot.getTextChannelById(configString("channelId"));
            if (channel != null) guild = channel.getGuild();

            guild.updateCommands().addCommands(Commands.slash("boop", "Does the boop thing.")).queue();
        }
        catch (Throwable err) {err.printStackTrace();}
        if (bot == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PluginManager pm = getServer().getPluginManager();
        PlayerListener playerListener = new Players();

        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Highest, this);

        getCommand("discordbridge").setExecutor(new Reload());

        sendDCMsg(configString("msg.serverStart"));
    }

    @Override
    public void onDisable() {
        if (bot != null) {
            guild.updateCommands().queue();
            sendDCMsg(configString("msg.serverStop"));
            bot.shutdownNow();
        }
    }

    public void sendDCMsg(String msg) {
        if (!channel.canTalk() || channel == null) return;
        channel.sendMessage(cleanFormat(msg)).queue();
    }

    // Config
    public String configString(String path) {return getConfiguration().getString(path);}
    public boolean configBool(String path) {return getConfiguration().getBoolean(path, true);}

    public void configSetup() {
        getConfiguration().load();
        getConfiguration().getString("token", "");
        getConfiguration().getString("channelId", "0000000000000000000");
        getConfiguration().getBoolean("discordToMinecraft", true);
        getConfiguration().getString("msg.serverStart", "**Server online!**");
        getConfiguration().getString("msg.serverStop", "**Server offline!**");
        getConfiguration().getString("msg.discordToMinecraftFormat", "§9[Discord] §r(user)§7: (msg)");
        getConfiguration().save();
    }

    // Extras
    public static String cleanFormat(String txt) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < txt.length(); i++) {
            if (txt.charAt(i) == '§') i++; else result.append(txt.charAt(i));
        }
        return result.toString();
    }
}