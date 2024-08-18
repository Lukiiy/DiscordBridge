package me.lukiiy.discordBridge.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class MemberHelper {
    public static boolean hasRole(@NotNull Member member, @NotNull Role role) {
        return member.getRoles().contains(role);
    }
}
