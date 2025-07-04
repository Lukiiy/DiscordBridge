package me.lukiiy.discordBridge.event;

import me.lukiiy.discordBridge.api.DiscordContext;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class BridgeDiscordReceiveEvent extends Event implements Cancellable {
    private boolean cancelled = false;
    private final Member member;
    private Message message;
    private DiscordContext context;

    public BridgeDiscordReceiveEvent(DiscordContext context, Member member, Message message) {
        super("BridgeDiscordReceiveEvent");
        this.context = context;
        this.member = member;
        this.message = message;
    }

    public Member getMember() {
        return member;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public DiscordContext getContext() {
        return context;
    }

    public void setContext(DiscordContext context) {
        this.context = context;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}