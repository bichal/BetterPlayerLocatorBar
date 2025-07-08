package net.bichal.bplb.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.bichal.bplb.util.Constants;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ServerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("bplb-server.json").toFile();
    private static ServerConfig instance;

    public int positionUpdateRateTicks = 2;
    public double positionChangeThreshold = 0.1;
    public int maxPlayersPerUpdate = 50;
    public boolean enablePositionPrediction = true;
    public int cleanupIntervalTicks = 600;

    public static ServerConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static ServerConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ServerConfig config = GSON.fromJson(reader, ServerConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (IOException e) {
                Constants.LOGGER.error("Could not read optimized server config", e);
            }
        }
        ServerConfig config = new ServerConfig();
        config.save();
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            Constants.LOGGER.error("Could not save optimized server config", e);
        }
    }
}
