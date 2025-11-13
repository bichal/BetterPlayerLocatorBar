package net.bichal.bplb.server.datapack;

import net.bichal.bichalutils.util.Logger;
import net.bichal.bplb.server.ServerConfig;
import net.bichal.bplb.util.Constants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DatapackExtractor {
    private static final String DATAPACK_NAME = Constants.MOD_NAME_SHORT.toLowerCase();

    public static void extractDatapack(MinecraftServer server) {
        if (!ServerConfig.getInstance().autoExtractDatapack()) {
            return;
        }

        try {
            Path worldPath = server.getSavePath(WorldSavePath.ROOT);
            Path datapacksPath = worldPath.resolve("datapacks").resolve(DATAPACK_NAME);
            if (Files.exists(datapacksPath.resolve("pack.mcmeta"))) {
                Logger.debug("[{}] Datapack already exists, skipping extraction");
                return;
            }

            Logger.info("Extracting datapack to {}", datapacksPath);
            Files.createDirectories(datapacksPath);
            createDatapackFiles(datapacksPath);

            Logger.info("Datapack extracted successfully! Run /reload to activate it.");

        } catch (IOException e) {
            Logger.error("[{}] Failed to extract datapack: {}", Constants.MOD_NAME_SHORT, e.getMessage());
        }
    }

    private static void createDatapackFiles(Path root) throws IOException {
        // pack.mcmeta
        createFile(root.resolve("pack.mcmeta"), """
                {
                  {
                  "pack": {
                    "pack_format": 48,
                    "description": "Datapack Version for Better Player Locator Bar"
                  }
                }
                """);

        // data/minecraft/tags/function/load.json
        Path loadTag = root.resolve("data/minecraft/tags/function");
        Files.createDirectories(loadTag);
        createFile(loadTag.resolve("load.json"), """
                {
                  "values": [
                    "bplb:setup"
                  ]
                }
                """);

        // data/minecraft/tags/function/tick.json
        createFile(loadTag.resolve("tick.json"), """
                {
                  "values": [
                    "bplb:tick"
                  ]
                }
                """);

        // data/bplb/function/
        Path functions = root.resolve("data/bplb/function");
        Files.createDirectories(functions);

        // setup.mcfunction
        createFile(functions.resolve("setup.mcfunction"), """
                scoreboard objectives add bplb_system dummy "BPLB System"
                scoreboard objectives add bplb_x dummy "BPLB X"
                scoreboard objectives add bplb_y dummy "BPLB Y"
                scoreboard objectives add bplb_z dummy "BPLB Z"
                scoreboard players set #bplb_enabled bplb_system 1
                scoreboard players set #bplb_tick bplb_system 0
                tellraw @a {"text":"[BPLB] Datapack initialized!","color":"green"}
                """);

        // tick.mcfunction
        createFile(functions.resolve("tick.mcfunction"), """
                execute unless score #bplb_enabled bplb_system matches 1 run return 0
                scoreboard players add #bplb_tick bplb_system 1
                execute if score #bplb_tick bplb_system matches 2.. run function bplb:update_positions
                execute if score #bplb_tick bplb_system matches 2.. run scoreboard players set #bplb_tick bplb_system 0
                """);

        // update_positions.mcfunction
        createFile(functions.resolve("update_positions.mcfunction"), """
                execute as @a store result score @s bplb_x run data get entity @s Pos[0] 10
                execute as @a store result score @s bplb_y run data get entity @s Pos[1] 10
                execute as @a store result score @s bplb_z run data get entity @s Pos[2] 10
                """);

        // enable.mcfunction
        createFile(functions.resolve("enable.mcfunction"), """
                scoreboard players set #bplb_enabled bplb_system 1
                tellraw @s {"text":"[BPLB] Datapack enabled","color":"green"}
                """);

        // disable.mcfunction
        createFile(functions.resolve("disable.mcfunction"), """
                scoreboard players set #bplb_enabled bplb_system 0
                tellraw @s {"text":"[BPLB] Datapack disabled","color":"yellow"}
                """);

        // uninstall.mcfunction
        createFile(functions.resolve("uninstall.mcfunction"), """
                scoreboard objectives remove bplb_system
                scoreboard objectives remove bplb_x
                scoreboard objectives remove bplb_y
                scoreboard objectives remove bplb_z
                tellraw @a {"text":"[BPLB] Datapack uninstalled","color":"red"}
                """);
    }

    private static void createFile(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
