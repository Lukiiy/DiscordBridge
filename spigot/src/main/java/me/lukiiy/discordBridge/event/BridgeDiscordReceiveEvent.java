package me.lukiiy.discordBridge.event;

import me.lukiiy.discordBridge.api.DiscordContext;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BridgeDiscordReceiveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private final @Nullable Member member;
    private @NotNull Message message;
    private @NotNull DiscordContext context;

    public BridgeDiscordReceiveEvent(@NotNull DiscordContext context, @Nullable Member member, @NotNull Message message) {
        this.context = context;
        this.member = member;
        this.message = message;
    }

    public @Nullable Member getMember() {
        return member;
    }

    public @NotNull Message getMessage() {
        return message;
    }

    public void setMessage(@NotNull Message message) {
        this.message = message;
    }

    public @NotNull DiscordContext getContext() {
        return context;
    }

    public void setContext(@NotNull DiscordContext context) {
        this.context = context;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}