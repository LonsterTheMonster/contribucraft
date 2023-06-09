package com.contribucraft;

import java.io.File;
import java.io.InputStream;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
class LangSetup {
    private JavaPlugin plugin;

    private static String resourcePath;

    LangSetup(JavaPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        String requestedLang = config.getString("lang");
        requestedLang = requestedLang + (requestedLang.endsWith(".yml") ? "" : ".yml");
        resourcePath = "lang" + File.separator + requestedLang;
        if (resourcePath.contains("\\"))
            resourcePath = resourcePath.replace("\\", "/");
        File langConfig = new File(plugin.getDataFolder(), resourcePath);
        if (!langConfig.exists()) {
            InputStream in = plugin.getResource(resourcePath);
            plugin.getLogger().warning("Cannot find " + resourcePath + " in config directory.");
            if (null == in) {
                plugin.getLogger().info("We could not find " + resourcePath + " in jar file, using default english.");
                resourcePath = "lang" + File.separator + "en.yml";
            } else {
                plugin.getLogger().info("Found " + resourcePath + " in resources directory, so we'll use that.");
            }
            copyJarResources();
        }
    }

    YamlConfiguration loadConfig() {
        return YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), resourcePath));
    }

    private void copyJarResources() {
        try {
            this.plugin.saveResource(resourcePath, false);
        } catch (Exception e) {
            this.plugin.getLogger().warning(e.getMessage());
        }
    }
}
