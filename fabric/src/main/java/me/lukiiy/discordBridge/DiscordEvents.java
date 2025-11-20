package me.lukiiy.discordBridge;

import me.lukiiy.discordBridge.api.CommandPlate;
import me.lukiiy.discordBridge.api.DiscordContext;
import me.lukiiy.discordBridge.api.MessageParts;
import me.lukiiy.uniStyle.UniParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;

public class DiscordEvents extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        DiscordBridge instance = DiscordBridge.getInstance();
        AtomicReference<DiscordContext> context = new AtomicReference<>(instance.getContext());
        Config config = DiscordBridge.getInstance().getConfig();
        UniParser parser = UniParser.getDEFAULT();

        if (e.getChannel() != context.get().getChannel()) return;

        Member member = e.getMember();
        AtomicReference<Message> msg = new AtomicReference<>(e.getMessage());

        MessageParts parts = MessageParts.from(
                config.getOrDefault("messages.minecraft.format", "").replace("(user)", DiscordBridge.getInstance().miniSerializableName(member)),
                config.getOrDefault("messages.minecraft.prefix", ""),
                member,
                msg.get(),
                context.get().getBot(),
                config.getBoolean("discord.ignoreBots"),
                !config.getOrDefault("messages.minecraft.reply.default", "").isBlank(),
                config.getOrDefault("messages.minecraft.reply.default", ""),
                config.getBoolean("messages.minecraft.reply.ignoreBot")
        );

        if (parts == null) return;

        Component prefix = parts.getPrefix().isBlank() ? Component.empty() : Component.Serializer.fromJson(parser.serialize(parts.getPrefix()));
        Component content = DSerialMoj.reformatComponent(Component.Serializer.fromJson(parser.serialize(parts.getContent())));
        Component formatted = Component.empty().append(prefix).append(content).append(" ").append(DSerialMoj.joinWith(Component.literal(" "), DSerialMoj.listAttachments(msg.get())));

        DiscordBridge.getInstance().playerCast(formatted);
    }

    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent e) {
        DiscordBridge instance = DiscordBridge.getInstance();

        CommandPlate cmd = instance.getContext().getCommand(e.getName());
        if (cmd == null) return;

        cmd.interaction(e);
        DiscordBridge.LOGGER.info("[Discord] {} issued server command: /{}", e.getMember(), e.getFullCommandName());
    }

    @Override
    public void onThreadMemberJoin(@NotNull ThreadMemberJoinEvent e) {
        ThreadChannel thread = e.getThread();
        if (!thread.isPublic() || thread.isArchived()) return;

        if (Duration.between(thread.getTimeCreated(), OffsetDateTime.now()).toMinutes() < 1) {
            DiscordBridge instance = DiscordBridge.getInstance();
            String msg = instance.getConfig().get("messages.minecraft.threadCreation");
            if (msg == null || msg.isBlank()) return;

            Component fMsg = Component.Serializer.fromJson(UniParser.getDEFAULT().serialize(msg.replace("(name)", thread.getName())));
            DiscordBridge.getInstance().playerCast(fMsg);
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent e) {
        TextChannel sysChannel = e.getGuild().getSystemChannel();
        DiscordBridge instance = DiscordBridge.getInstance();

        if (sysChannel != null && sysChannel == instance.getContext().getChannel()) {
            String msg = instance.getConfig().get("messages.minecraft.userJoin");
            if (msg == null || msg.isBlank()) return;

            Component fMsg = Component.Serializer.fromJson(UniParser.getDEFAULT().serialize(msg.replace("(name)", instance.miniSerializableName(e.getMember()))));
            DiscordBridge.getInstance().playerCast(fMsg);
        }
    }
}
