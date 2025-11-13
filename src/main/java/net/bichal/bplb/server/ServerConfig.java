package net.bichal.bplb.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.bichal.bichalutils.util.Logger;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ServerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("bplb-server.json").toFile();
    private static volatile ServerConfig instance;

    private int positionUpdateRateTicks;
    private double positionChangeThreshold;
    private int cleanupIntervalTicks;
    private double maxRelevantDistance;
    private int threadPoolSize;
    private String preset;
    private boolean useDatapackFallback;
    private boolean autoExtractDatapack;

    public ServerConfig() {
        applyPreset("recommended");
    }

    private ServerConfig(int positionUpdateRateTicks, double positionChangeThreshold, int cleanupIntervalTicks, double maxRelevantDistance, int threadPoolSize, String preset, boolean useDatapackFallback, boolean autoExtractDatapack) {
        this.positionUpdateRateTicks = positionUpdateRateTicks;
        this.positionChangeThreshold = positionChangeThreshold;
        this.cleanupIntervalTicks = cleanupIntervalTicks;
        this.maxRelevantDistance = maxRelevantDistance;
        this.threadPoolSize = threadPoolSize;
        this.preset = preset;
        this.useDatapackFallback = useDatapackFallback;
        this.autoExtractDatapack = autoExtractDatapack;
    }

    public static ServerConfig getInstance() {
        if (instance == null) {
            synchronized (ServerConfig.class) {
                if (instance == null) {
                    instance = load();
                }
            }
        }
        return instance;
    }

    private static ServerConfig load() {
        if (CONFIG_FILE.exists()) {
            try {
                String json = Files.readString(CONFIG_FILE.toPath());
                ServerConfig config = GSON.fromJson(json, ServerConfig.class);
                if (config != null) {
                    config.updatePresetIfNeeded();
                    return config;
                }
            } catch (IOException e) {
                Logger.error("Could not read server config", e);
            }
        }
        ServerConfig config = new ServerConfig();
        config.save();
        return config;
    }

    private static ServerConfig createFromPreset(String presetName) {
        return switch (presetName.toLowerCase()) {
            case "minimal" -> new ServerConfig(5, 0.5, 1200, 256.0, 1, "minimal", true, true);
            case "recommended" ->
                    new ServerConfig(2, 0.1, 600, 512.0, Math.max(1, Runtime.getRuntime().availableProcessors() / 2), "recommended", true, true);
            case "insane" ->
                    new ServerConfig(1, 0.05, 300, 1024.0, Runtime.getRuntime().availableProcessors(), "insane", true, true);
            default -> null;
        };
    }

    private void updatePresetIfNeeded() {
        if (!matchesPreset(this.preset)) {
            this.preset = "custom";
            Logger.info("Server config doesn't match preset, switched to 'custom'");
        }
    }

    private boolean matchesPreset(String presetName) {
        ServerConfig presetConfig = createFromPreset(presetName);
        if (presetConfig == null) return false;

        return this.positionUpdateRateTicks == presetConfig.positionUpdateRateTicks && Math.abs(this.positionChangeThreshold - presetConfig.positionChangeThreshold) < 0.001 && this.cleanupIntervalTicks == presetConfig.cleanupIntervalTicks && Math.abs(this.maxRelevantDistance - presetConfig.maxRelevantDistance) < 0.1 && this.threadPoolSize == presetConfig.threadPoolSize;
    }

    public void applyPreset(String presetName) {
        ServerConfig presetConfig = createFromPreset(presetName);
        if (presetConfig != null) {
            this.positionUpdateRateTicks = presetConfig.positionUpdateRateTicks;
            this.positionChangeThreshold = presetConfig.positionChangeThreshold;
            this.cleanupIntervalTicks = presetConfig.cleanupIntervalTicks;
            this.maxRelevantDistance = presetConfig.maxRelevantDistance;
            this.threadPoolSize = presetConfig.threadPoolSize;
            this.preset = presetName;
            this.useDatapackFallback = presetConfig.useDatapackFallback;
            this.autoExtractDatapack = presetConfig.autoExtractDatapack;
        }
    }

    public void save() {
        try {
            File parent = CONFIG_FILE.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            Files.writeString(CONFIG_FILE.toPath(), GSON.toJson(this));
            Logger.info("Server config saved (preset: {})", preset);
        } catch (IOException e) {
            Logger.error("Could not save server config", e);
        }
    }

    public int positionUpdateRateTicks() {
        return positionUpdateRateTicks;
    }

    public double positionChangeThreshold() {
        return positionChangeThreshold;
    }

    public int cleanupIntervalTicks() {
        return cleanupIntervalTicks;
    }

    public double maxRelevantDistance() {
        return maxRelevantDistance;
    }

    public int threadPoolSize() {
        return threadPoolSize;
    }

    public String preset() {
        return preset;
    }

    public boolean useDatapackFallback() {
        return useDatapackFallback;
    }

    public boolean autoExtractDatapack() {
        return autoExtractDatapack;
    }

    public void setPositionUpdateRateTicks(int value) {
        this.positionUpdateRateTicks = Math.max(1, Math.min(value, 100));
        updatePresetIfNeeded();
    }

    public void setPositionChangeThreshold(double value) {
        this.positionChangeThreshold = Math.max(0.01, Math.min(value, 10.0));
        updatePresetIfNeeded();
    }

    public void setMaxRelevantDistance(double value) {
        this.maxRelevantDistance = Math.max(64.0, Math.min(value, 2048.0));
        updatePresetIfNeeded();
    }

    public void setThreadPoolSize(int value) {
        this.threadPoolSize = Math.max(1, Math.min(value, Runtime.getRuntime().availableProcessors()));
        updatePresetIfNeeded();
    }

    public void setUseDatapackFallback(boolean value) {
        this.useDatapackFallback = value;
    }
}
