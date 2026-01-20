package fr.nihilis.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("scoreboard-notifier.json");

    private static ModConfig config;

    public static ModConfig load() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                config = new ModConfig();
                save();
            } else {
                try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                    config = GSON.fromJson(reader, ModConfig.class);
                }
            }
            return config;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }

    public static ModConfig get() {
        return config;
    }

    private ConfigManager() {}
}
