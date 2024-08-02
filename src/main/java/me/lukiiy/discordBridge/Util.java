package me.lukiiy.discordBridge;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class Util {
    public static String cleanFormat(String txt) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < txt.length(); i++) if (txt.charAt(i) == '§') i++; else s.append(txt.charAt(i));
        return s.toString();
    }

    public static boolean doesMemberHaveRole(@NotNull Member member, @NotNull Role role) {
        return member.getRoles().contains(role);
    }

    public static String getMemberHEXColor(@NotNull Member member) {
        Color color = member.getColor();
        if (color == null) color = Color.WHITE;
        int value = (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        return String.format("#%06X", value);
    }
}
