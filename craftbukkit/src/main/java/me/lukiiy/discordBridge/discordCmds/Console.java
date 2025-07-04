package me.lukiiy.discordBridge.discordCmds;

import me.lukiiy.discordBridge.DiscordBridge;
import me.lukiiy.discordBridge.api.CommandPlate;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Console implements CommandPlate {
    private final ConsoleCommandSender consoleSender;

    public Console(Server server) {
        consoleSender = new ConsoleCommandSender(server);
    }

    @NotNull
    @Override
    public CommandData command() {
        return Commands.slash("console", "Executes a command as console.")
                .addOptions(new OptionData(OptionType.STRING, "command", "Command").setRequired(true))
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM);
    }

    @Override
    public void interaction(CommandInteraction i) {
        DiscordBridge instance = DiscordBridge.getInstance();
        String cmd = Objects.requireNonNull(i.getOption("command")).getAsString();
        Member member = i.getMember();

        if (member == null || !instance.getContext().hasConsoleAdminRole(member)) {
            i.reply("You don't have access to this command.").setEphemeral(true).queue();
            return;
        }

        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        String fCmd = cmd;

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> Bukkit.getServer().dispatchCommand(consoleSender, fCmd));
        instance.getServer().getLogger().info(member.getEffectiveName() + " executed \"/" + cmd + "\" as console.");
        i.reply("Executed \"" + cmd + "\" as console.").setEphemeral(true).queue();
    }
}
