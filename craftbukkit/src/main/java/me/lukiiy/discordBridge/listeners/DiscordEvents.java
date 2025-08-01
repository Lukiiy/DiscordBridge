package me.lukiiy.discordBridge.listeners;

import me.lukiiy.discordBridge.DiscordBridge;
import me.lukiiy.discordBridge.api.CommandPlate;
import me.lukiiy.discordBridge.api.DiscordContext;
import me.lukiiy.discordBridge.api.MessageParts;
import me.lukiiy.discordBridge.api.serialize.DSerial;
import me.lukiiy.discordBridge.event.BridgeDiscordReceiveEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.util.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class DiscordEvents extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        DiscordBridge instance = DiscordBridge.getInstance();
        AtomicReference<DiscordContext> context = new AtomicReference<>(instance.getContext());
        Configuration config = instance.getConfiguration();

        if (e.getChannel() != context.get().getChannel()) return;

        Server server = Bukkit.getServer();
        Member member = e.getMember();
        AtomicReference<Message> msg = new AtomicReference<>(e.getMessage());

        server.getScheduler().scheduleSyncDelayedTask(instance, () -> {
            BridgeDiscordReceiveEvent event = new BridgeDiscordReceiveEvent(context.get(), member, msg.get());
            server.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            context.set(event.getContext());
            msg.set(event.getMessage());

            MessageParts parts = MessageParts.from(
                    config.getString("messages.minecraft.format", ""),
                    config.getString("messages.minecraft.prefix", ""),
                    member,
                    msg.get(),
                    context.get().getBot(),
                    config.getBoolean("discord.ignoreBots", false),
                    !config.getString("messages.minecraft.reply.default", "").isEmpty(),
                    config.getString("messages.minecraft.reply.default", ""),
                    config.getBoolean("messages.minecraft.reply.ignoreBot", true)
            );

            if (parts == null) return;

            String formatted = parts.getPrefix() + DSerial.fromDiscord(parts.getContent()) + DSerial.listAttachments(msg.get()).stream().collect(Collectors.joining(" ", " ", ""));

            instance.commonSend(formatted);
        });
    }

    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent e) {
        DiscordBridge instance = DiscordBridge.getInstance();

        CommandPlate cmd = instance.getContext().getCommand(e.getName());
        if (cmd == null) return;

        cmd.interaction(e);
        instance.getServer().getLogger().info("[Discord] " + Objects.requireNonNull(e.getMember()).getEffectiveName() + " issued server command: /" + e.getFullCommandName());
    }

    @Override
    public void onThreadMemberJoin(@NotNull ThreadMemberJoinEvent e) {
        ThreadChannel thread = e.getThread();
        if (!thread.isPublic() || thread.isArchived()) return;

        if (Duration.between(thread.getTimeCreated(), OffsetDateTime.now()).toMinutes() < 1) {
            DiscordBridge instance = DiscordBridge.getInstance();
            String msg = instance.getConfiguration().getString("msg.threadCreation");
            if (msg == null || DiscordBridge.isBlank(msg)) return;

            msg = msg.replace("(name)", thread.getName());

            instance.commonSend(msg);
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent e) {
        TextChannel sysChannel = e.getGuild().getSystemChannel();
        DiscordBridge instance = DiscordBridge.getInstance();

        if (sysChannel != null && sysChannel == instance.getContext().getChannel()) {
            String msg = instance.getConfiguration().getString("messages.minecraft.userJoin");
            if (msg == null || DiscordBridge.isBlank(msg)) return;

            msg = msg.replace("(user)", e.getMember().getEffectiveName());

            instance.commonSend(msg);
        }
    }
}
