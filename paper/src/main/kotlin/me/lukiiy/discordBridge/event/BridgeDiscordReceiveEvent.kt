package me.lukiiy.discordBridge.event

import me.lukiiy.discordBridge.api.DiscordContext
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class BridgeDiscordReceiveEvent(@JvmField var context: DiscordContext, val member: Member?, @JvmField var message: Message) : Event(), Cancellable {
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