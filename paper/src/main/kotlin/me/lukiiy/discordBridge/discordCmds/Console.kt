package me.lukiiy.discordBridge.discordCmds

import me.lukiiy.discordBridge.DiscordBridge
import me.lukiiy.discordBridge.api.CommandPlate
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.bukkit.Bukkit

class Console : CommandPlate {
    override fun command(): CommandData {
        return Commands.slash("console", "Executes a command as console.")
            .addOptions(OptionData(OptionType.STRING, "command", "Command").setRequired(true))
            .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM)
    }

    override fun interaction(interaction: CommandInteraction) {
        val instance = DiscordBridge.getInstance()
        var cmd = interaction.getOption("command")?.asString
        val member = interaction.member

        if (member == null || !instance.context!!.hasConsoleAdminRole(member)) {
            interaction.reply("You don't have access to this command.").setEphemeral(true).queue()
            return
        }

        if (cmd!!.startsWith("/")) cmd = cmd.substring(1)

        instance.server.globalRegionScheduler.run(instance) { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd) }
        instance.componentLogger.info(member.effectiveName + " executed \"/" + cmd + "\" as console.")
        interaction.reply("Executed \"$cmd\" as console.").setEphemeral(true).queue()
    }
}
