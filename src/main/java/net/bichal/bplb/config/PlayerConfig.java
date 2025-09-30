package net.bichal.bplb.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.bichal.bplb.util.Constants;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.UUID;

public class PlayerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File PLAYERS_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("Better Player Locator Bar/Players")
            .toFile();

    public String playerName;
    public UUID playerUuid;
    public Integer iconSize;
    public String dotType;
    public String iconBorderStyle;
    public String iconBorderType;
    public Boolean inheritBorderColor;
    public String arrowType;
    public Integer color;
    public Float nameplateScale;
    public String nameBorderStyle;
    public String textureHeadOverride;

    public static PlayerConfig load(UUID uuid) {
        File file = new File(PLAYERS_DIR, uuid.toString() + ".json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                return GSON.fromJson(reader, PlayerConfig.class);
            } catch (Exception e) {
                Constants.LOGGER.error("Error loading player config for {}", uuid, e);
            }
        }
        return null;
    }

    public void save() {
        PLAYERS_DIR.mkdirs();
        File file = new File(PLAYERS_DIR, playerUuid.toString() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        } catch (Exception e) {
            Constants.LOGGER.error("Error saving player config", e);
        }
    }

    public void delete() {
        File file = new File(PLAYERS_DIR, playerUuid.toString() + ".json");
        file.delete();
    }
}