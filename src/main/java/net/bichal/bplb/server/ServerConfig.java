package net.bichal.bplb.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.bichal.bplb.util.Constants;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public record ServerConfig(int positionUpdateRateTicks, double positionChangeThreshold, int cleanupIntervalTicks, double maxRelevantDistance, int threadPoolSize, String preset) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("bplb-server.json").toFile();
    private static final Object LOCK = new Object();
    private static volatile ServerConfig instance;

    public ServerConfig() {
        this(2, 0.1, 0, 512.0, Math.max(1, Runtime.getRuntime().availableProcessors() / 2), "recommended");
    }

    public static ServerConfig minimal() {
        return new ServerConfig(5, 0.5, 1200, 256.0, 1, "minimal");
    }

    public static ServerConfig recommended() {
        return new ServerConfig(2, 0.1, 600, 512.0, Math.max(1, Runtime.getRuntime().availableProcessors() / 2), "recommended");
    }

    public static ServerConfig insane() {
        return new ServerConfig(1, 0.05, 300, 1024.0, Runtime.getRuntime().availableProcessors(), "insane");
    }


    @SuppressWarnings("unused") // future use
    public static ServerConfig fromPreset(String presetName) {
        return switch (presetName.toLowerCase()) {
            case "minimal" -> minimal();
            case "insane" -> insane();
            default -> recommended();
        };
    }

    public static ServerConfig getInstance() {
        ServerConfig result = instance;
        if (result == null) {
            synchronized (LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = load();
                }
            }
        }
        return result;
    }

    private static @NotNull ServerConfig load() {
        if (CONFIG_FILE.exists()) {
            try {
                byte[] data = Files.readAllBytes(CONFIG_FILE.toPath());
                if (data.length > 0) {
                    String json = new String(data);
                    ServerConfig config = GSON.fromJson(json, ServerConfig.class);
                    if (config != null) {
                        return config;
                    }
                }
            } catch (IOException e) {
                Constants.LOGGER.error("Could not read server config", e);
            }
        }

        ServerConfig config = new ServerConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            File parent = CONFIG_FILE.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                Constants.LOGGER.error("Failed to create config directory");
                return;
            }
            String json = GSON.toJson(this);
            Files.write(CONFIG_FILE.toPath(), json.getBytes());
        } catch (IOException e) {
            Constants.LOGGER.error("Could not save server config", e);
        }
    }
}
