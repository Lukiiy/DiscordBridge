package me.lukiiy.discordBridge.cmds;

import me.lukiiy.discordBridge.DiscordBridge;
import me.lukiiy.discordBridge.api.DiscordContext;
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Main implements CommandExecutor, TabExecutor {
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
        Audience sender = instance.getAudiences().sender(commandSender);
        DiscordContext context = instance.getContext();

        String arg = strings.length < 1 ? "help" : strings[0].toLowerCase();

        switch (arg) {
            case "reload":
                instance.reloadConfig();
                sender.sendMessage(Component.text("DiscordBridge messages & settings Reload complete!").color(NamedTextColor.GREEN));
                return true;

            case "bc":
            case "bcast":
            case "broadcast":
                if (strings.length == 1) {
                    sender.sendMessage(Component.text("Usage: /dcbridge broadcast <msg>").color(NamedTextColor.RED));
                    return true;
                }

                String msg = String.join(" ", strings).substring(strings[0].length() + 1);

                context.sendMessage(msg);
                instance.getLogger().info("[Broadcast] " + msg);
                sender.sendMessage(Component.text("Message broadcasted!").color(NamedTextColor.GREEN));
                return true;

            case "dccmds":
            case "discordcmds":
            case "discordcommands":
                sender.sendMessage(Component.text("Available Discord commands:").color(DSerialAdvnt.getBridgeBlue()));
                context.getCommands().keySet().forEach(cmd -> sender.sendMessage(Component.text("•").color(DSerialAdvnt.getBridgeList()).appendSpace().append(Component.text(cmd).color(NamedTextColor.GOLD))));
                return true;

            case "mainc":
            case "mainconnection":
                sender.sendMessage(Component.text("Main connection info:").color(DSerialAdvnt.getBridgeBlue()));
                DiscordContext.getContextSummary(context).forEach((i, v) -> sender.sendMessage(Component.text("•").color(DSerialAdvnt.getBridgeList()).appendSpace().append(Component.text(i + ": ").color(NamedTextColor.YELLOW)).append(Component.text(v))));
                return true;

            default:
                sender.sendMessage(Component.text("DCBridge Subcommands:").color(DSerialAdvnt.getBridgeBlue()));
                subCmds.forEach((cmd, desc) -> sender.sendMessage(Component.text("•").color(DSerialAdvnt.getBridgeList()).appendSpace().append(Component.text(cmd).color(NamedTextColor.GOLD)).append(Component.text(" → ")).append(Component.text(desc))));
                return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            String input = strings[0].toLowerCase();

            return Main.subCmds.keySet().stream()
                    .flatMap(key -> Arrays.stream(key.split(",\\s*"))) // comma split & space
                    .map(String::toLowerCase)
                    .filter(it -> it.startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
