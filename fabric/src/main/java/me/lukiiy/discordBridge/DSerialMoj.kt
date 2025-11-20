package me.lukiiy.discordBridge

import me.lukiiy.discordBridge.DSerialMoj.fromDiscord
import me.lukiiy.discordBridge.DSerialMoj.toDiscord
import net.dv8tion.jda.api.entities.Message
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.network.chat.contents.LiteralContents

object DSerialMoj {
    @JvmStatic val styles = mapOf(
        "bold" to "**",
        "italic" to "*",
        "underlined" to "__",
        "strikethrough" to "~~",
        "obfuscated" to "||"
    )

    @JvmStatic val reverseStyles = styles.entries.associate { it.value to it.key }
    @JvmStatic val regex = Regex("(${styles.values.joinToString("|") { Regex.escape(it) }})(.+?)\\1")

    /**
     * Convert a component into a Discord markdown-usable string
     * @see [fromDiscord]
     * @return A Discord Markdown formatted string
     */
    @JvmStatic
    fun toDiscord(component: Component): String {
        val str = StringBuilder()

        fun process(c: Component) {
            val style = c.style
            val text = c.string

            if (text.isNotEmpty()) {
                var formatted = text
                if (style.isObfuscated) formatted = "||$formatted||"
                if (style.isStrikethrough) formatted = "~~$formatted~~"
                if (style.isUnderlined) formatted = "__${formatted}__"
                if (style.isItalic) formatted = "*$formatted*"
                if (style.isBold) formatted = "**$formatted**"

                str.append(formatted)
            }

            c.siblings.forEach { process(it) }
        }

        process(component)
        return str.toString()
    }

    /**
     * Convert a component into a Discord markdown-usable string
     * @see [fromDiscord]
     * @return A Discord Markdown formatted string
     */
    @JvmStatic fun toDiscord(string: String): String = toDiscord(Component.literal(string))

    /**
     * Convert a Discord Markdown formatted string into a component
     * @see [toDiscord]
     * @return A component
     */
    @JvmStatic
    fun fromDiscord(string: String): Component {
        val input = string
            .replace(Regex("""\|\|(.+?)\|\|""")) { "|${it.groupValues[1]}|" }
            .replace(Regex("```([^`]+?)```")) { "{${it.groupValues[1]}}" }
            .replace(Regex("`([^`]+?)`")) { "{${it.groupValues[1]}}" }
            .replace("\n", " ")

        var component = Component.literal("")
        var current = Style.EMPTY
        val str = StringBuilder()

        fun appendText() {
            if (str.isNotEmpty()) {
                component = component.append(Component.literal(str.toString()).setStyle(current))
                str.clear()
            }
        }

        var i = 0
        while (i < input.length) {
            var matched = false

            for ((markdown, tag) in reverseStyles) {
                if (input.startsWith(markdown, i)) {
                    appendText()

                    current = when (tag) {
                        "bold" -> current.withBold(!current.isBold)
                        "italic" -> current.withItalic(!current.isItalic)
                        "underlined" -> current.withUnderlined(!current.isUnderlined)
                        "strikethrough" -> current.withStrikethrough(!current.isStrikethrough)
                        "obfuscated" -> current.withObfuscated(!current.isObfuscated)
                        else -> current
                    }

                    i += markdown.length
                    matched = true
                    break
                }
            }

            if (!matched) {
                str.append(input[i])
                i++
            }
        }
        appendText()

        return component
    }

    @JvmStatic
    fun reformatComponent(component: Component): Component {
        val text = when (val contents = component.contents) {
            is LiteralContents -> contents.text()
            else -> ""
        }

        val styledText = if (text.isNotEmpty()) fromDiscord(text) else Component.empty()
        var result = Component.empty().withStyle(component.style).append(styledText)

        component.siblings.forEach { result = result.append(reformatComponent(it)) }
        return result
    }

    /**
     * Gets and returns a list of little information about a [Message]'s attachments and embends.
     * Not intended for external use.
     * @return A list of somewhat usable information
     */
    @JvmStatic
    fun listAttachments(message: Message): List<Component> {
        val result = mutableListOf<Component>()
        val color = TextColor.fromRgb(0x40566b)
        val embeds = message.embeds

        if (embeds.isNotEmpty()) result.add(Component.literal("[${embeds.size} embed file(s)]").withStyle { it.withColor(color) })

        message.attachments.forEach {
            result.add(Component.literal("[${it.fileName.substringAfterLast('.', "file")} file]").withStyle { s -> s.withColor(color).withClickEvent(net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_URL, it.url)) })
        }

        return result
    }

    @JvmStatic
    fun joinWith(separator: Component, components: MutableList<Component>): Component? {
        if (components.isEmpty()) return Component.empty()

        var result = components[0]
        for (i in 1..<components.size) result = result.copy().append(separator.copy()).append(components[i])

        return result
    }

    @JvmStatic val bridgeBlue = TextColor.fromRgb(0x647ff8)
    @JvmStatic val bridgeFaint = TextColor.fromRgb(0x7175a3)
    @JvmStatic val bridgeList = TextColor.fromRgb(0x6d7494)
}
