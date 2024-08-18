package me.lukiiy.discordBridge.util;

import me.lukiiy.discordBridge.DCBridge;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.NotNull;

public class BotHelper {
    public static OnlineStatus getStatus(@NotNull String info) {
        try {return OnlineStatus.valueOf(info);}
        catch (IllegalArgumentException e) {
            DCBridge.log("\"" + info + "\" is not a valid status. Please change it in your config.yml.");
            return OnlineStatus.ONLINE;
        }
    }

    public static Activity getActivity(@NotNull String string) {
        String[] parts = string.split("\\s+", 2);
        String detail = parts.length > 1 ? parts[1] : string;

        try {return Activity.of(Activity.ActivityType.valueOf(parts[0].toUpperCase()), detail);}
        catch (IllegalArgumentException e) {
            DCBridge.log("\"" + string + "\" is not a valid activity type. Please change it in your config.yml.");
            return Activity.of(Activity.ActivityType.PLAYING, detail);
        }
    }
}
