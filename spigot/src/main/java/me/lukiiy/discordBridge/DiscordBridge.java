package me.lukiiy.discordBridge;

import me.clip.placeholderapi.PlaceholderAPI;
import me.lukiiy.discordBridge.api.DiscordContext;
import me.lukiiy.discordBridge.cmds.Main;
import me.lukiiy.discordBridge.discordCmds.Console;
import me.lukiiy.discordBridge.listeners.DefaultEvents;
import me.lukiiy.discordBridge.listeners.DiscordEvents;
import me.lukiiy.discordBridge.utils.BotHelper;
import me.lukiiy.discordBridge.utils.MemberHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.Duration;

public class DiscordBridge extends JavaPlugin {
    private DiscordContext context;
    private BukkitAudiences audiences;
    private boolean PlaceholderAPIHook = false;

    @Override
    public void onEnable() {
        setupConfig();

        audiences = BukkitAudiences.create(this);

        getServer().getPluginManager().registerEvents(new DefaultEvents(), this);
        getCommand("discordbridge").setExecutor(new Main());

        // Bot
        String token = getConfig().getString("discord.token");
        if (token == null || token.isEmpty()) {
            getLogger().warning("Insert the bot token in config.yml and then restart the server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        BukkitScheduler scheduler = Bukkit.getScheduler();

        final long channelId = getConfig().getLong("discord.channelId");
        final long consoleAccessRole = getConfig().getLong("discord.consoleRoleId");
        final String activityKey = getConfig().getString("discord.activity", "");
        final String statusKey = getConfig().getString("discord.status", "");
        final String startMsg = getConfig().getString("messages.discord.start", "");

        scheduler.runTaskAsynchronously(this, () -> {
            try {
                context = DiscordContext.init(token, channelId, consoleAccessRole);
                final JDA bot = context.getBot();

                Activity activity = null;
                try {
                    activity = BotHelper.getActivity(activityKey);
                } catch (Exception e) {
                    getLogger().info(e.getMessage());
                }

                bot.getPresence().setPresence(OnlineStatus.fromKey(statusKey), activity);
                context.sendMessage(startMsg);

                if (context.getConsoleAdminRole() != null) context.addCommands(new Console());
                bot.addEventListener(new DiscordEvents());
            } catch (Exception e) {
                getLogger().severe(e.getMessage());
                scheduler.runTask(this, () -> getServer().getPluginManager().disablePlugin(this));
            }
        });

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) PlaceholderAPIHook = true;
    }

    @Override
    public void onDisable() {
        if (context != null) {
            if (getConfig().getBoolean("discord.shutdown.clearCommands")) context.clearCommands();

            context.sendMessage(getConfig().getString("messages.discord.stop", ""));
            context.shutdown(Duration.ofSeconds(getConfig().getLong("discord.shutdown.timeLimit", 3)));

            context = null;
        }

        if (audiences != null) audiences.close();
    }

    public static DiscordBridge getInstance() {
        return JavaPlugin.getPlugin(DiscordBridge.class);
    }

    public DiscordContext getContext() {
        return context;
    }

    public BukkitAudiences getAudiences() {
        return audiences;
    }

    public String miniSerializableName(Member member) {
        return getConfig().getBoolean("discord.useMemberNameColor") ? "<color:" + MemberHelper.getHexColor(member) + ">" + member.getEffectiveName() + "</color>" : member.getEffectiveName();
    }

    public void commonSend(Component msg) {
        getAudiences().players().sendMessage(msg);
        getLogger().info(PlainTextComponentSerializer.plainText().serialize(msg));
    }

    // Config
    public void setupConfig() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    // Hooks
    public String parsePlaceholders(Player player, String message) {
        if (!PlaceholderAPIHook || player == null || message == null) return message;
        return PlaceholderAPI.setPlaceholders(player, message);
    }

    // Java backports
    public static boolean isBlank(String input) {
        return input == null || input.isEmpty() || input.chars().allMatch(Character::isWhitespace);
    }
}
