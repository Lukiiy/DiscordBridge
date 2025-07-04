package me.lukiiy.discordBridge.api.serialize

import me.lukiiy.discordBridge.api.serialize.DSerial.fromDiscord
import me.lukiiy.discordBridge.api.serialize.DSerial.toDiscord
import net.dv8tion.jda.api.entities.Message


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
    fun fromDiscord(input: String): String {
        return regex.replace(input) { match ->
            val code = reverseStyles[match.groupValues[1]] ?: return@replace match.value
            "§$code$match.groupValues[2]§r"
        }
    }

    /**
     * Gets and returns a list of little information about a [Message]'s attachments and embends.
     * Not intended for external use.
     * @return A list of somewhat usable information
     */
    @JvmStatic
    fun listAttachments(message: Message): List<String> {
        val result = mutableListOf<String>()

        message.attachments.forEach { result.add("§8[${it.fileName.substringAfterLast('.', "file")} file]§f") }
        if (message.embeds.isNotEmpty()) result.add(" §8[${message.embeds.size} embed file(s)]§f")

        return result
    }
}