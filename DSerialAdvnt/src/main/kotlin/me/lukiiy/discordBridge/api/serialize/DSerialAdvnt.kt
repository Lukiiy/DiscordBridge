package me.lukiiy.discordBridge.api.serialize

import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt.fromDiscord
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt.toDiscord
import net.dv8tion.jda.api.entities.Message
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

object DSerialAdvnt {
    @JvmStatic val MINI = MiniMessage.miniMessage()

    @JvmStatic val styles = mapOf(
        "b" to "**",
        "bold" to "**",
        "i" to "*",
        "italic" to "*",
        "u" to "__",
        "underlined" to "__",
        "s" to "~~",
        "strikethrough" to "~~",
        "obfuscated" to "||"
    )

    @JvmStatic val reverseStyles = styles.entries.associate { it.value to it.key }
    @JvmStatic val regex = Regex("(${styles.values.joinToString("|") { Regex.escape(it) }})(.+?)\\1") // A pattern that can match discord md styles

    /**
     * Convert a component into a Discord markdown-usable string
     * @see [fromDiscord]
     * @return A Discord Markdown formatted string
     */
    @JvmStatic
    fun toDiscord(component: Component): String {
        var mini = MINI.serialize(component)

        styles.forEach { (tag, markdown) -> // replace formatting tags with markdown
            mini = mini.replace("<$tag>", markdown).replace("</$tag>", markdown)
        }

        return PlainTextComponentSerializer.plainText().serialize(MINI.deserialize(mini))
    }

    /**
     * Convert a component into a Discord markdown-usable string
     * @see [fromDiscord]
     * @return A Discord Markdown formatted string
     */
    @JvmStatic fun toDiscord(string: String): String = toDiscord(LegacyComponentSerializer.legacySection().deserialize(string))

    /**
     * Convert a Discord Markdown formatted string into a component
     * @see [toDiscord]
     * @return A component
     */
    @JvmStatic
    fun fromDiscord(string: String): Component {
        val lightGray = "#bfbfbf"
        val gray = "#787878"
        var input = string

        input = input.replace(Regex("""\|\|(.+?)\|\|""")) { // add hover text to obfuscated text
            val text = it.groupValues[1]

            "<hover:show_text:'$text'><obfuscated>$text</obfuscated></hover>"
        }

        input = regex.replace(input) { // replace markdown with formatting tags
            reverseStyles[it.groupValues[1]]?.let { tag -> "<$tag>${it.groupValues[2]}</$tag>" } ?: it.value
        }

        // code blocks
        input = input.replace(Regex("```([^`]+?)```")) { match -> "<c:$gray><i>{{</c>${match.groupValues[1]}<c:$gray>}}</i></c>" }

        // code
        input = input.replace(Regex("`([^`]+?)`")) { match -> "<c:$gray>{</c>${match.groupValues[1]}<c:$gray>}</c>" }

        // replace new lines with nothing
        input = input.replace("\n", " ").ifBlank { "" }

        return MINI.deserialize(input)
    }

    /**
     * Gets and returns a list of little information about a [Message]'s attachments and embends.
     * Not intended for external use.
     * @return A list of somewhat usable information
     */
    @JvmStatic
    fun listAttachments(message: Message): List<Component> { // TODO: Merge with #fromDiscord
        val result = mutableListOf<Component>()
        val color = TextColor.color(0x40566b)
        val embeds = message.embeds

        if (embeds.isNotEmpty()) result.add(Component.text("[${embeds.size} embed file(s)]", color))

        message.attachments.forEach {
            result.add(Component.text("[${it.fileName.substringAfterLast('.', "file")} file]").color(color).clickEvent(ClickEvent.openUrl(it.url)))
        }

        return result
    }

    // Default colors
    @JvmStatic val bridgeBlue = TextColor.fromHexString("#647ff8")
    @JvmStatic val bridgeFaint = TextColor.fromHexString("#7175a3")
    @JvmStatic val bridgeList = TextColor.fromHexString("#6d7494")
}