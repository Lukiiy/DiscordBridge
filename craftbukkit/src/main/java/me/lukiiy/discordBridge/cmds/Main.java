package me.lukiiy.discordBridge.cmds;

import me.lukiiy.discordBridge.DiscordBridge;
import me.lukiiy.discordBridge.api.DiscordContext;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class Main implements CommandExecutor {
    private static final Map<String, String> subCmds = new LinkedHashMap<>();

    static {
        subCmds.put("reload", "Reload the plugin");
        subCmds.put("broadcast, bcast, bc", "Broadcasts a message to Discord");
        subCmds.put("dccmds, discordcmds", "Show the registered commands for the bot");
        subCmds.put("mainc, mainconnection", "Show some information about the main connection");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        DiscordBridge instance = DiscordBridge.getInstance();
        DiscordContext context = instance.getContext();

        String arg = strings.length < 1 ? "help" : strings[0].toLowerCase();

        switch (arg) {
            case "reload":
                instance.getConfiguration().load();
                commandSender.sendMessage("§aDiscordBridge messages & settings reload complete.");
                break;

            case "bc":
            case "bcast":
            case "broadcast":
                if (strings.length == 1) {
                    commandSender.sendMessage("§cUsage: /dcbridge bc <msg>");
                    return true;
                }

                String msg = String.join(" ", strings).substring(strings[0].length() + 1);

                context.sendMessage(msg);
                instance.getServer().getLogger().info("[Broadcast] " + msg);
                commandSender.sendMessage("§aMessage broadcasted!");
                break;

            case "dccmds":
            case "discordcmds":
            case "discordcommands":
                commandSender.sendMessage("§9Available Discord commands:");
                context.getCommands().keySet().forEach(cmd -> commandSender.sendMessage("§8- §6" + cmd));
                break;

            case "mainc":
            case "mainconnection":
                commandSender.sendMessage("§9Main connection info:");
                DiscordContext.getContextSummary(context).forEach((i, v) -> commandSender.sendMessage("§8- §e" + i + ":§8 " + v));
                break;

            default:
                commandSender.sendMessage("§9DCBridge Subcommands:");
                subCmds.forEach((cmd, desc) -> commandSender.sendMessage("§8- §6" + cmd + "§8 -> " + desc));
                break;
        }

        return true;
    }
}
