package me.lukiiy.betadiscordbridge.listeners;

import me.lukiiy.betadiscordbridge.DiscordBridge;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.CloseCode;
import org.bukkit.Bukkit;

public class Discord extends ListenerAdapter {
    DiscordBridge bridge = DiscordBridge.inst;

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (!bridge.configBool("discordToMinecraft") || !e.getChannel().equals(bridge.channel)) return;

        Member member = e.getMember();
        if (member == null || member.getUser().isBot() || member.getUser().isSystem()) return;

        String msg = e.getMessage().getContentDisplay();
        if (msg.isEmpty()) return;
        String formatted = bridge.configString("msg.discordToMinecraftFormat")
                        .replace("(user)", member.getEffectiveName())
                        .replace("(msg)", msg);
        Bukkit.getServer().broadcastMessage(formatted);
        DiscordBridge.inst.logger.info(DiscordBridge.prefix + formatted);
    }

    @Override
    public void onReady(ReadyEvent e) {
        DiscordBridge.inst.logger.info(DiscordBridge.prefix + "It's working!");
    }

    @Override
    public void onSessionDisconnect(SessionDisconnectEvent e) {
        CloseCode code = e.getCloseCode();
        DiscordBridge.inst.logger.warning(DiscordBridge.prefix + "Session disconnected...");
        DiscordBridge.inst.logger.info(DiscordBridge.prefix + "Code: " + code.getCode() + " – " + code.getMeaning());
    }

    @Override
    public void onSessionResume(SessionResumeEvent e) {
        DiscordBridge.inst.logger.warning(DiscordBridge.prefix + "It's working again!");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if (e.getName().equals("boop")) e.reply("**Boop!**").setEphemeral(true).queue();
    }
}
