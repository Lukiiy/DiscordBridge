package me.lukiiy.discordBridge.listeners;

import me.lukiiy.discordBridge.DCBridge;
import me.lukiiy.discordBridge.Util;
import me.lukiiy.discordBridge.api.CommandManager;
import me.lukiiy.discordBridge.api.CommandPlate;
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
    DCBridge bridge = DCBridge.getInstance();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (!bridge.configBool("discordToMinecraft") || !e.getChannel().equals(DCBridge.getChannel())) return;

        Member member = e.getMember();
        if (member == null || member.getUser().isBot() || member.getUser().isSystem()) return;

        String msg = e.getMessage().getContentDisplay();
        if (msg.isEmpty()) return;

        String name = bridge.configBool("discord.useMemberNameColor") ? "<color:" + Util.getMemberHEXColor(member) + ">" + member.getEffectiveName() + "</color>" : member.getEffectiveName();

        Component formatted = DCBridge.mm.deserialize(bridge.configString("format.dcPrefix") + " " + bridge.configString("format.dc")
                .replace("(user)", name)
                .replace("(msg)", msg));

        DCBridge.bukkitAud.players().sendMessage(formatted);
        DCBridge.console.sendMessage(formatted);
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
        CommandPlate cmd = CommandManager.list().get(e.getName());
        if (cmd != null) cmd.interaction(e);
    }
}
