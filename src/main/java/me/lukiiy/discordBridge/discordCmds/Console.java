package me.lukiiy.discordBridge.discordCmds;

import me.lukiiy.discordBridge.DCBridge;
import me.lukiiy.discordBridge.util.GenericHelper;
import me.lukiiy.discordBridge.api.CommandPlate;
import me.lukiiy.discordBridge.util.MemberHelper;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bukkit.Bukkit;

public class Console implements CommandPlate {
    @Override
    public CommandData command() {
        return Commands.slash("console", "Executes a command as console.")
                .addOptions(new OptionData(OptionType.STRING, "command", "Command").setRequired(true))
                .setGuildOnly(true);
    }

    @Override
    public void interaction(CommandInteraction i) {
        Member member = i.getMember();
        if (member == null || !MemberHelper.hasRole(member, DCBridge.consoleAdminRole)) {
            i.reply("You don't have access to this command.").setEphemeral(true).queue();
            return;
        }
        String cmd = i.getOption("command").getAsString();
        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        String command = cmd;
        Bukkit.getScheduler().runTask(DCBridge.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        DCBridge.log(GenericHelper.miniString(DCBridge.configString("format.dcPrefix") + " " + MemberHelper.getCoolName(member) + " executed \"/" + cmd + "\" as console."));
        i.reply("Executed \"" + cmd + "\" as console.").setEphemeral(true).queue();
    }
}
