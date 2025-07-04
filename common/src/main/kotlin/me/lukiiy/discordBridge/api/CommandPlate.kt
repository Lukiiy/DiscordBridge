package me.lukiiy.discordBridge.api

import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.commands.build.CommandData

/**
 * A simple Discord command plate
 */
interface CommandPlate {
    fun command(): CommandData

    fun interaction(interaction: CommandInteraction)
}
