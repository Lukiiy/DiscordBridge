package me.lukiiy.discordBridge.listeners;

import me.lukiiy.discordBridge.DCBridge;
import me.lukiiy.discordBridge.util.GenericHelper;
import me.lukiiy.discordBridge.api.CommandPlate;
import me.lukiiy.discordBridge.util.MemberHelper;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class Discord extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (!DCBridge.configBool("discord.chatToMinecraft") || !e.getChannel().equals(DCBridge.getChannel())) return;

        Member member = e.getMember();
        if (member == null || member.getUser().isBot() || member.getUser().isSystem()) return;

        String msg = e.getMessage().getContentDisplay();
        if (msg.isEmpty()) return;

        Component formatted = GenericHelper.miniMessage.deserialize(DCBridge.configString("format.dcPrefix") + " " + DCBridge.configString("format.dc")
                .replace("(user)", MemberHelper.getCoolName(member))
                .replace("(msg)", msg));

        GenericHelper.audience.players().sendMessage(formatted);
        DCBridge.log(formatted);
    }

    @Override
    public void onReady(@NotNull ReadyEvent e) {DCBridge.log("It's working!");}

    @Override
    public void onSessionDisconnect(@NotNull SessionDisconnectEvent e) {
        if (e.getCloseCode() == null) return;
        DCBridge.log("Session disconnected... Error code: " + e.getCloseCode().getCode());
    }

    @Override
    public void onSessionResume(@NotNull SessionResumeEvent e) {DCBridge.log("It's working again!");}

    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent e) {
        CommandPlate cmd = DCBridge.getCommandMap().get(e.getName());
        if (cmd != null) cmd.interaction(e);
    }
}
