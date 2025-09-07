package me.lukiiy.discordBridge

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class Config(fileName: String, modName: String?) {
    val properties: Properties = Properties()
    private val file: File
    private val modName: String?

    init {
        val confDir = File("DiscordBridge")
        if (!confDir.exists()) confDir.mkdirs()

        this.file = File(confDir, fileName)
        this.modName = modName

        load()
    }

    fun load() {
        if (!file.exists()) save()

        FileReader(file).use {
            properties.load(it)
        }
    }

    fun save() {
        FileWriter(file).use { writer ->
            properties.store(writer, "$modName Config")
        }
    }

    fun has(key: String?): Boolean = properties.getProperty(key) != null

    fun set(key: String?, value: String?) {
        properties.setProperty(key, value)
        save()
    }

    fun setIfAbsent(key: String?, value: String?) {
        if (has(key)) return
        set(key, value)
    }

    fun get(key: String?): String? {
        return properties.getProperty(key)
    }

    fun getBoolean(key: String?): Boolean = get(key).equals("true", ignoreCase = true)

    fun getLong(key: String?): Long = try {
        get(key)?.toLong() ?: 0L
    } catch (_: NumberFormatException) {
        0L
    }

    fun getOrDefault(key: String?, defaultKey: String?): String? = properties.getProperty(key, defaultKey)
}
