package me.lukiiy.discordBridge.util;

import me.lukiiy.discordBridge.DCBridge;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class MemberHelper {
    public static boolean hasRole(@NotNull Member member, @NotNull Role role) {
        return member.getRoles().contains(role);
    }

    public static String getHexColor(@NotNull Member member) {
        Color color = member.getColor();
        if (color == null) color = Color.WHITE;
        int value = (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        return String.format("#%06X", value);
    }

    public static String getCoolName(@NotNull Member member) {
        return DCBridge.configBool("discord.useMemberNameColor") ? "<color:" + MemberHelper.getHexColor(member) + ">" + member.getEffectiveName() + "</color>" : member.getEffectiveName();
    }

    public static String replaceMentions(String msg) {
        return replaceMentions(msg, DCBridge.getGuild());
    }
    public static String replaceMentions(String msg, Guild guild) {
        StringBuilder result = new StringBuilder();
        String[] words = msg.split("\\s+");

        for (String word : words) {
            if (word.startsWith("@")) {
                String username = word.substring(1);
                Member member = guild.getMemberByTag(username);
                if (member != null) word = member.getAsMention();
            }
            if (!result.isEmpty()) result.append(" ");
            result.append(word);
        }

        return result.toString();
    }
}
