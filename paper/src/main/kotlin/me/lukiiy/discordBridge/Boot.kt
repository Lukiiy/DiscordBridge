package me.lukiiy.discordBridge

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import me.lukiiy.discordBridge.cmds.Main

class Boot : PluginBootstrap {
    override fun bootstrap(ctx: BootstrapContext) {
        ctx.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(Main.register(), "The main command for DiscordBridge!", listOf("dcbridge", "dcb"))
        }
    }
}