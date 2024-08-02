package me.lukiiy.discordBridge.api;

import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface CommandPlate {
    CommandData command();
    default void interaction(CommandInteraction i) {
        i.reply("Hey there!").setEphemeral(true).queue();
    }
}
