package me.lukiiy.discordBridge.cmds;

import me.lukiiy.discordBridge.DCBridge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Reload implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        DCBridge.getInstance().setupConfig();
        commandSender.sendMessage("§aDiscordBridge messages & settings reload complete.");
        return true;
    }
}
