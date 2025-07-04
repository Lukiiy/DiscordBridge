package me.lukiiy.discordBridge.utils

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import java.awt.Color

object MemberHelper {
    @JvmStatic
    fun Member.hasRole(role: Role): Boolean = this.roles.contains(role)

    @JvmStatic
    fun Member.getHexColor(): String {
        val color = this.color ?: Color.WHITE
        val value = (color.red shl 16) or (color.green shl 8) or color.blue

        return String.format("#%06X", value)
    }

    @JvmStatic
    fun fixMentions(msg: String, guild: Guild): String {
        return Regex("""@(\w+)""").replace(msg) { result ->
            val member = guild.members.firstOrNull { it.effectiveName.equals(result.groupValues[1], ignoreCase = true) }

            member?.asMention ?: result.value
        }
    }
}