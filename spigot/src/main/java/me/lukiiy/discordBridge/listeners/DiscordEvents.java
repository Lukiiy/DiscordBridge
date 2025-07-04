package me.lukiiy.discordBridge.listeners;

import me.lukiiy.discordBridge.DiscordBridge;
import me.lukiiy.discordBridge.api.CommandPlate;
import me.lukiiy.discordBridge.api.DiscordContext;
import me.lukiiy.discordBridge.api.MessageParts;
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;

public class DiscordEvents extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        DiscordBridge instance = DiscordBridge.getInstance();
        AtomicReference<DiscordContext> context = new AtomicReference<>(instance.getContext());
        FileConfiguration config = instance.getConfig();

        if (e.getChannel() != context.get().getChannel()) return;

        Member member = e.getMember();
        AtomicReference<Message> msg = new AtomicReference<>(e.getMessage());

        Bukkit.getScheduler().runTask(instance, (task) -> {
            BridgeDiscordReceiveEvent event = new BridgeDiscordReceiveEvent(context.get(), member, msg.get());
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                task.cancel();
                return;
            }

            context.set(event.getContext());
            msg.set(event.getMessage());

            MessageParts parts = MessageParts.from(
                    config.getString("messages.minecraft.format", "").replace("(user)", instance.miniSerializableName(member)),
                    config.getString("messages.minecraft.prefix", ""),
                    member,
                    msg.get(),
                    context.get().getBot(),
                    config.getBoolean("discord.ignoreBots"),
                    !DiscordBridge.isBlank(config.getString("messages.minecraft.reply.default", "")),
                    config.getString("messages.minecraft.reply.default", ""),
                    config.getBoolean("messages.minecraft.reply.ignoreBot")
            );

            if (parts == null) return;

            Component prefix = DiscordBridge.isBlank(parts.getPrefix()) ? Component.empty() : DSerialAdvnt.getMINI().deserialize(parts.getPrefix());
            Component content = DSerialAdvnt.fromDiscord(parts.getContent());
            Component formatted = Component.empty().append(prefix).append(content).appendSpace().append(Component.join(JoinConfiguration.spaces(), DSerialAdvnt.listAttachments(msg.get())));

            instance.getAudiences().players().sendMessage(formatted);
            instance.getLogger().info(PlainTextComponentSerializer.plainText().serialize(formatted));
        });
    }

    @Override
    public void onReady(@NotNull ReadyEvent e) {
        DiscordBridge.getInstance().getLogger().info("It's working!");
    }

    @Override
    public void onSessionDisconnect(@NotNull SessionDisconnectEvent e) {
        if (e.getCloseCode() == null) return;

        DiscordBridge.getInstance().getLogger().warning("Session disconnected... Error code: " + e.getCloseCode().getCode());
    }

    @Override
    public void onSessionResume(@NotNull SessionResumeEvent e) {
        DiscordBridge.getInstance().getLogger().info("It's working again!");
    }

    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent e) {
        DiscordBridge instance = DiscordBridge.getInstance();

        CommandPlate cmd = instance.getContext().getCommand(e.getName());
        if (cmd == null) return;

        cmd.interaction(e);
        instance.getLogger().info(PlainTextComponentSerializer.plainText().serialize(DSerialAdvnt.getMINI().deserialize("[Discord] " + instance.miniSerializableName(e.getMember()) + " issued server command: /" + e.getFullCommandName())));
    }

    @Override
    public void onThreadMemberJoin(@NotNull ThreadMemberJoinEvent e) {
        ThreadChannel thread = e.getThread();
        if (!thread.isPublic() || thread.isArchived()) return;

        if (Duration.between(thread.getTimeCreated(), OffsetDateTime.now()).toMinutes() < 1) {
            DiscordBridge instance = DiscordBridge.getInstance();
            String msg = instance.getConfig().getString("messages.minecraft.threadCreation");
            if (msg == null || DiscordBridge.isBlank(msg)) return;

            Component fMsg = DSerialAdvnt.getMINI().deserialize(msg.replace("(name)", thread.getName()));

            instance.getAudiences().players().sendMessage(fMsg);
            instance.getLogger().info(PlainTextComponentSerializer.plainText().serialize(fMsg));
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent e) {
        TextChannel sysChannel = e.getGuild().getSystemChannel();
        DiscordBridge instance = DiscordBridge.getInstance();

        if (sysChannel != null && sysChannel == instance.getContext().getChannel()) {
            String msg = instance.getConfig().getString("messages.minecraft.userJoin");
            if (msg == null || DiscordBridge.isBlank(msg)) return;

            Component fMsg = DSerialAdvnt.getMINI().deserialize(msg.replace("(name)", instance.miniSerializableName(e.getMember())));

            instance.getAudiences().players().sendMessage(fMsg);
            instance.getLogger().info(PlainTextComponentSerializer.plainText().serialize(fMsg));
        }
    }
}
