package me.lukiiy.discordBridge;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Config {
    private final Properties properties = new Properties();
    private final File file;
    private final String modName;

    public Config(String fileName, String modName) {
        File confDir = FabricLoader.getInstance().getConfigDir().resolve(modName).toFile();
        if (!confDir.exists()) confDir.mkdirs();

        this.file = new File(confDir, fileName);
        this.modName = modName;

        load();
    }

    public void load() {
        if (!file.exists()) save();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            properties.store(writer, modName + " Config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean has(String key) {
        return properties.getProperty(key) != null;
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
        save();
    }

    public void setIfAbsent(String key, String value) {
        if (has(key)) return;

        set(key, value);
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public boolean getBoolean(String key) {
        String value = get(key);

        return value != null && value.equalsIgnoreCase("true");
    }

    public long getLong(String key) {
        try {
            String value = get(key);

            return value != null ? Long.parseLong(value) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}