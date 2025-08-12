package me.lukiiy.discordBridge.api.serialize

import me.lukiiy.discordBridge.api.serialize.DSerial.fromDiscord
import me.lukiiy.discordBridge.api.serialize.DSerial.toDiscord
import net.dv8tion.jda.api.entities.Message
import java.awt.Color
import kotlin.math.sqrt


object DSerial {
    @JvmStatic
    val styles = mapOf(
        'l' to "**",
        'o' to "*",
        'n' to "__",
        'm' to "~~",
        'k' to "||"
    )

    @JvmStatic val reverseStyles = styles.entries.associate { it.value to it.key }
    @JvmStatic val regex = Regex("(${styles.values.joinToString("|") { Regex.escape(it) }})(.+?)\\1")

    /**
     * Convert a legacy string into a Discord markdown-usable string
     * @see [fromDiscord]
     * @return A Discord Markdown formatted string
     */
    @JvmStatic
    fun toDiscord(input: String): String {
        val result = StringBuilder()
        var i = 0

        while (i < input.length) {
            if (input[i] == '§' && i + 1 < input.length) {
                val markdown = styles[input[i + 1]]

                if (markdown != null) {
                    val start = i + 2
                    val end = input.indexOf('§', start).takeIf { it != -1 } ?: input.length

                    result.append(markdown).append(input.substring(start, end)).append(markdown)
                    i = end

                    continue
                }
            }
            result.append(input[i])
            i++
        }

        return result.toString().replace(Regex("§."), "")
    }

    /**
     * Convert a Discord Markdown formatted string into a legacy string
     * @see [toDiscord]
     * @return A legacy string
     */
    @JvmStatic
    fun fromDiscord(string: String): String {
        val lightGray = "§7"
        val gray = "§8"
        var input = string

        input = input.replace(Regex("""\|\|(.+?)\|\|""")) { "$lightGray!!§r§k${it.groupValues[1]}§r$lightGray!!§r" } // add hover text to obfuscated text

        input = regex.replace(input) { // replace markdown with formatting tags
            val code = reverseStyles[it.groupValues[1]] ?: return@replace it.value

            "§$code${it.groupValues[2]}§r"
        }

        // code blocks
        input = input.replace(Regex("```([^`]+?)```")) { "$gray§o{{§r${it.groupValues[1]}$gray§o}}§r" }

        // code
        input = input.replace(Regex("`([^`]+?)`")) { "$gray{${it.groupValues[1]}$gray}§r" }

        input = input.replace("\n", " ").ifBlank { "" }

        return input
    }

    /**
     * Gets and returns a list of little information about a [Message]'s attachments and embends.
     * Not intended for external use.
     * @return A list of somewhat usable information
     */
    @JvmStatic
    fun listAttachments(message: Message): List<String> { // TODO: Merge with #fromDiscord
        val result = mutableListOf<String>()

        message.attachments.forEach { result.add("§8[${it.fileName.substringAfterLast('.', "file")} file]§f") }
        if (message.embeds.isNotEmpty()) result.add(" §8[${message.embeds.size} embed file(s)]§f")

        return result
    }

    /**
     * Downsample a hex color string to a chat color code
     */
    @JvmStatic
    fun minecraftSampledHex(hex: String): String {
        val colorMap = mapOf(
            '0' to Color.BLACK, // Black
            '1' to Color(0, 0, 170), // Dark Blue
            '2' to Color(0, 170, 0), // Dark Green
            '3' to Color(0, 170, 170), // Dark Aqua
            '4' to Color(170, 0, 0), // Dark Red
            '5' to Color(170, 0, 170), // Dark Purple
            '6' to Color(255, 170, 0), // Gold
            '7' to Color(170, 170, 170), // Gray
            '8' to Color(85, 85, 85), // Dark Gray
            '9' to Color(85, 85, 255), // Blue
            'a' to Color(85, 255, 85), // Green
            'b' to Color(85, 255, 255), // Aqua
            'c' to Color(255, 85, 85), // Red
            'd' to Color(255, 85, 255), // Light Purple
            'e' to Color(255, 255, 85), // Yellow
            'f' to Color.WHITE // White
        )

        val target = runCatching { Color.decode(hex) }.getOrElse { Color.WHITE }

        return colorMap.minByOrNull { (_, color) ->
            val dr = target.red - color.red
            val dg = target.green - color.green
            val db = target.blue - color.blue

            dr * dr + dg * dg + db * db
        }?.let { "§${it.key}" } ?: "§f"
    }
}