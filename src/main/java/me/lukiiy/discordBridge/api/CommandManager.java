package me.lukiiy.discordBridge.api;

import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private static final Map<String, CommandPlate> commands = new HashMap<>();

    public static Map<String, CommandPlate> list() {return commands;}
    public static void add(CommandPlate cmd) {commands.put(cmd.command().getName(), cmd);}
    public static void remove(CommandPlate cmd) {commands.remove(cmd.command().getName());}
    public static void remove(String cmdName) {commands.remove(cmdName);}

    public static void load(CommandListUpdateAction c) {c.addCommands(commands.values().stream().map(CommandPlate::command).toList()).queue();}
}
