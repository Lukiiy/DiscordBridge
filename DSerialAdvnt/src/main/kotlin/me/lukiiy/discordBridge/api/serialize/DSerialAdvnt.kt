package me.lukiiy.discordBridge.api.serialize

import me.lukiiy.discordBridge.BridgeDefaults
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt.fromDiscord
import me.lukiiy.discordBridge.api.serialize.DSerialAdvnt.toDiscord
import net.dv8tion.jda.api.entities.Message
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
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
        val parts = mutableListOf<Pair<String, Set<TextDecoration>>>()

        component.iterateStyled { text, style ->
            if (text.isNotEmpty()) parts.add(text to TextDecoration.entries.filter { style.hasDecoration(it) }.toSet())
        }

        return buildString {
            var i = 0

            while (i < parts.size) {
                val (text, decs) = parts[i]
                val grouped = StringBuilder(text)

                while (i + 1 < parts.size && parts[i + 1].second == decs) grouped.append(parts[++i].first)

                val tags = decs.mapNotNull { styles[it.name.lowercase()] }
                append(tags.joinToString("") + grouped + tags.reversed().joinToString(""))

                i++
            }
        }
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
    @JvmStatic fun fromDiscord(s: String): Component = MINI.deserialize(regex.replace(s) { match -> reverseStyles[match.groupValues[1]]?.let { "<$it>${match.groupValues[2]}</$it>" } ?: match.value })

    private fun Component.iterateStyled(consumer: (String, Style) -> Unit) {
        when (this) {
            is TextComponent -> consumer(content(), style())
            is TranslatableComponent -> consumer(key(), style())
        }

        children().forEach { it.iterateStyled(consumer) }
    }

    /**
     * Gets and returns a list of little information about a [Message]'s attachments and embends.
     * Not intended for external use.
     * @return A list of somewhat usable information
     */
    @JvmStatic
    fun listAttachments(message: Message): List<Component> {
        val result = mutableListOf<Component>()
        val color = TextColor.color(0x40566b)
        val embeds = message.embeds

        if (embeds.isNotEmpty()) result.add(Component.text("[${embeds.size} embed file(s)]", color))

        message.attachments.forEach {
            result.add(Component.text("[${it.fileName.substringAfterLast('.', "file")} file]").color(color).clickEvent(ClickEvent.openUrl(it.url)))
        }

        return result
    }

    // Colors from BridgeDefaults
    @JvmStatic val bridgeBlue = TextColor.fromHexString(BridgeDefaults.HEX_PRIMARY)
    @JvmStatic val bridgeFaint = TextColor.fromHexString(BridgeDefaults.FAINTED)
    @JvmStatic val bridgeList = TextColor.fromHexString(BridgeDefaults.LIST)
}