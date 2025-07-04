package me.lukiiy.discordBridge.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class BridgeMinecraftReceiveEvent extends Event implements Cancellable {
    private boolean cancel = false;
    private final Player player;
    private String message;

    public BridgeMinecraftReceiveEvent(Player player, String message) {
        super("BridgeMinecraftReceiveEvent");
        this.player = player;
        this.message = message;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}