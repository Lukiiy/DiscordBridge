package me.lukiiy.discordBridge.cmds;

import me.lukiiy.discordBridge.DCBridge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Broadcast implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 0) {
            commandSender.sendMessage("§cUsage: /dcb <msg>");
            return true;
        }
        String msg = String.join(" ", strings);
        DCBridge.sendDCMsg(msg);
        DCBridge.log(msg);
        return true;
    }
}
