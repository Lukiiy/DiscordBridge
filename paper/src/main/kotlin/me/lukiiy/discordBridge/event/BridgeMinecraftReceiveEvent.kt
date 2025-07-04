package me.lukiiy.discordBridge.event

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class BridgeMinecraftReceiveEvent(val player: Player?, @JvmField var message: Component) : Event(), Cancellable {
    private var cancel = false

    override fun isCancelled(): Boolean = cancel

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}