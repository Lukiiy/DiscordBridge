package me.lukiiy.discordBridge.api

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message

/**
 * Holds message data that the plugin uses in different ways depending on the version
 */
data class MessageParts(val prefix: String, val content: String) {
    companion object {
        /**
         * Build the prefix + content for a Discord → Minecraft bridge message.
         * Not intended for external use.
         *
         * @param format The format for the message; Blank will cause this to return null
         * @param prefixFormat The format for the prefix
         * @param member The sender; Null will cause this to return null
         * @param msg The message
         * @param bot The [JDA] (Bot)
         * @param ignoreBotMsgs Whether to ignore relaying messages from bots
         * @param showReply Enable the reply formatting feature
         * @param replySomeoneFormat The format used for when the sender replies to someone; Only used when showReply is true
         * @param replyIgnoreBot Whether to not display any 'reply indicator' if the sender is replying to the bot; Only used when showReply is true
         * @return A usable [MessageParts]
         */
        @JvmStatic
        @JvmOverloads
        fun from(format: String, prefixFormat: String = "", member: Member?, msg: Message, bot: JDA, ignoreBotMsgs: Boolean = true, showReply: Boolean = false, replySomeoneFormat: String? = "", replyIgnoreBot: Boolean = true): MessageParts? {
            if (format.isBlank() || member == null || member.user == bot.selfUser || ignoreBotMsgs && (member.user.isBot || member.user.isSystem)) return null

            val repliedTo = msg.referencedMessage?.author
            val reply = if (showReply && repliedTo != null) when {
                msg.author == repliedTo -> ""
                repliedTo == bot.selfUser && replyIgnoreBot -> ""
                else -> " ${replySomeoneFormat?.replace("(user)", repliedTo.effectiveName)}"
            } else ""

            val prefix = if (prefixFormat.isBlank()) "" else "${prefixFormat.replace("(userid)", member.id).replace("(id)", msg.id)} "
            val content = format.replace("(user)", member.effectiveName).replace("(reply)", reply).replace("(msg)", msg.contentDisplay.replace("\n", " ").ifBlank { "" })

            return MessageParts(prefix, content)
        }
    }
}