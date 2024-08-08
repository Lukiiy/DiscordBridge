package me.lukiiy.discordBridge.util;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GenericHelper {
    public static BukkitAudiences audience;
    public static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Component miniString(String txt) {return miniMessage.deserialize(txt);}

    public static String cleanFormat(String txt) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < txt.length(); i++) if (txt.charAt(i) == '§') i++; else s.append(txt.charAt(i));
        return s.toString();
    }
}
