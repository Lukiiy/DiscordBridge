package me.lukiiy.discordBridge;

import me.lukiiy.discordBridge.api.DiscordContext;
import me.lukiiy.discordBridge.utils.BotHelper;
import me.lukiiy.discordBridge.utils.MemberHelper;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordBridge implements ModInitializer {
    private static DiscordBridge INSTANCE;
    public static final Logger LOGGER = LoggerFactory.getLogger("DiscordBridge");
    private DiscordContext context = null;
    private MinecraftServer server = null;
    private Config config;

    @Override
    public void onInitialize() {
        INSTANCE = this;
        config = new Config("config.properties", "DiscordBridge");

        // default values values
        config.setIfAbsent("discord.token", "");
        config.setIfAbsent("discord.channelId", "1234567890123456789");
        config.setIfAbsent("discord.consoleRoleId", "1234567890123456789");
        config.setIfAbsent("discord.activity", "playing Minecraft");
        config.setIfAbsent("discord.status", "ONLINE");
        config.setIfAbsent("discord.playerEvents", "true");
        config.setIfAbsent("discord.useMemberNameColor", "true");
        config.setIfAbsent("discord.ignoreBots", "false");

        config.setIfAbsent("discord.shutdown.timeLimit", "3");
        config.setIfAbsent("discord.shutdown.clearCommands", "true");

        config.setIfAbsent("messages.discord.start", "**Server online!**");
        config.setIfAbsent("messages.discord.stop", "**Server offline!**");
        config.setIfAbsent("messages.discord.format", "<(user)> (msg)");

        config.setIfAbsent("messages.minecraft.prefix", "[hover:text=User: (userid)\\nMessage ID: (id);click=suggest:(id)][#647ff8][Discord][/]");
        config.setIfAbsent("messages.minecraft.format", "(user)[reset]:(reply) (msg)");
        config.setIfAbsent("messages.minecraft.threadCreation", "[color:#cf6eff]A new thread \"(name)\" has been created!");
        config.setIfAbsent("messages.minecraft.userJoin", "[yellow](user) has joined the discord server!");
        config.setIfAbsent("messages.minecraft.reply.default", "[italic][color:#7175a3]\\ (user)");
        config.setIfAbsent("messages.minecraft.reply.ignoreBot", "true");
        config.save();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void initBot(MinecraftServer server) {
        String token = config.get("discord.token");

        if (token == null || token.isBlank()) {
            LOGGER.info("Insert the bot token in config.properties and restart.");
            return;
        }

        this.server = server;

        try {
            context = DiscordContext.init(token, Long.parseLong(config.getOrDefault("discord.channelId", "0")), Long.parseLong(config.getOrDefault("discord.consoleRoleId", "0")));
            context.getBot().getPresence().setPresence(OnlineStatus.fromKey(config.getOrDefault("discord.status", "ONLINE")), BotHelper.getActivity(config.getOrDefault("discord.activity", "playing Minecraft")));
            context.sendMessage(config.getOrDefault("messages.discord.start", "Bot started"));
            context.getBot().addEventListener(new DiscordEvents());
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
    }

    public static DiscordBridge getInstance() {
        return INSTANCE;
    }

    public DiscordContext getContext() {
        return context;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public Config getConfig() {
        return config;
    }

    public void shutdown() {
        if (context == null) return;

        if (config.getBoolean("discord.shutdown.clearCommands")) context.clearCommands();
        context.sendMessage(config.getOrDefault("messages.discord.stop", "Bot stopping"));
        context.shutdown();

        context = null;
    }

    public String miniSerializableName(Member member) {
        return config.getBoolean("discord.useMemberNameColor") ? "[color:" + MemberHelper.getHexColor(member) + "]" + member.getEffectiveName() + "[reset]" : member.getEffectiveName();
    }

    public void sendNeutral(String msg, boolean priority) {
        if (context == null) return;

        DiscordBridge instance = DiscordBridge.getInstance();
        DiscordContext context = instance.getContext();

        if ((priority && !config.getBoolean("discord.playerEvents")) || msg.isBlank()) return;
        context.sendMessage(DSerialMoj.toDiscord(MemberHelper.fixMentions(msg, context.getGuild())));
    }

    public void playerCast(Component msg) {
        getServer().getPlayerList().broadcastSystemMessage(msg, false);
    }
}
