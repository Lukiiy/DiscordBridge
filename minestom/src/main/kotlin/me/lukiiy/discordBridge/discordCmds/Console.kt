package me.lukiiy.discordBridge.discordCmds

import me.lukiiy.discordBridge.Main
import me.lukiiy.discordBridge.api.CommandPlate
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.minestom.server.MinecraftServer

class Console : CommandPlate {
    override fun command(): CommandData = Commands.slash("console", "Executes a command as console.").addOptions(OptionData(OptionType.STRING, "command", "Command").setRequired(true)).setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM)

    override fun interaction(interaction: CommandInteraction) {
        var cmd = interaction.getOption("command")?.asString
        val member = interaction.member

        if (member == null || !Main.context!!.hasConsoleAdminRole(member)) {
            interaction.reply("You don't have access to this command.").setEphemeral(true).queue()
            return
        }

        if (cmd!!.startsWith("/")) cmd = cmd.substring(1)
        val cmdManager = MinecraftServer.getCommandManager()

        cmdManager.dispatcher.execute(cmdManager.consoleSender, cmd)
        MinecraftServer.LOGGER.info(member.effectiveName + " executed \"/" + cmd + "\" as console.")
        interaction.reply("Executed \"$cmd\" as console.").setEphemeral(true).queue()
    }
}
