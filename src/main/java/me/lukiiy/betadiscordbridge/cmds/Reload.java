package me.lukiiy.betadiscordbridge.cmds;

import me.lukiiy.betadiscordbridge.DiscordBridge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Reload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        DiscordBridge.inst.configSetup();
        commandSender.sendMessage("§aDiscordBridge Reload complete.");
        return true;
    }
}
