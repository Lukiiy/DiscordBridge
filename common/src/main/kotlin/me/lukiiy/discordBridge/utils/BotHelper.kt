package me.lukiiy.discordBridge.utils

import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity

/**
 * Utility functions related to the bot
 */
object BotHelper {
    /**
     * Try to retrieve a valid status from a config string.
     *
     * @param string Something like "ONLINE"
     *
     * @return A valid [OnlineStatus] or an exception.
     */
    @JvmStatic
    @Throws(Exception::class)
    fun getStatus(string: String): OnlineStatus {
        try {
            return OnlineStatus.valueOf(string.uppercase())
        } catch (e: IllegalArgumentException) {
            throw Exception("\"$string\" cannot generate a valid status.", e)
        }
    }

    /**
     * Try to get a valid [Activity] from a formatted string.
     *
     * @param string Something like "playing Minecraft"
     *
     * @return A valid [Activity] or an exception.
     */
    @JvmStatic
    @Throws(Exception::class)
    fun getActivity(string: String): Activity {
        val parts = string.split("\\s+".toRegex(), 2)
        val detail = if (parts.size > 1) parts[1] else string

        try {
            return Activity.of(Activity.ActivityType.valueOf(parts[0].uppercase()), detail)
        } catch (e: IllegalArgumentException) {
            throw Exception("\"$string\" cannot generate a valid activity.", e)
        }
    }
}