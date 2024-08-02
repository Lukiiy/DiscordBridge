package me.lukiiy.discordBridge.dccmds;

import me.lukiiy.discordBridge.DCBridge;
import me.lukiiy.discordBridge.Util;
import me.lukiiy.discordBridge.api.CommandPlate;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;

public class Console implements CommandPlate {
    DCBridge bridge = DCBridge.getInstance();

    @Override
    public CommandData command() {
        return Commands.slash("console", "Executes a command as console.")
                .addOptions(new OptionData(OptionType.STRING, "command", "Command").setRequired(true))
                .setGuildOnly(true);
    }

    @Override
    public void interaction(CommandInteraction i) {
        Member member = i.getMember();
        if (member == null || !Util.doesMemberHaveRole(member, DCBridge.consoleAdminRole)) {
            i.reply("You don't have access to this command.").setEphemeral(true).queue();
            return;
        }
        String name = bridge.configBool("discord.useMemberNameColor") ? "<color:" + Util.getMemberHEXColor(member) + ">" + member.getEffectiveName() + "</color>" : member.getEffectiveName();
        String cmd = i.getOption("command").getAsString();
        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        String command = cmd;
        Bukkit.getScheduler().runTask(bridge, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        DCBridge.log(DCBridge.mm.deserialize(bridge.configString("format.dcPrefix") + " " + name + " executed \"/" + cmd + "\" as console."));
        i.reply("Executed \"" + cmd + "\" as console.").setEphemeral(true).queue();
    }
}
