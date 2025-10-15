package net.bichal.bplb.client.render;

import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class AssetScanner {
    private static final Map<Identifier, Dimension> dimensionCache = new HashMap<>();

    public static List<String> getPlayerDots(ResourceManager resourceManager) {
        List<String> dots = new ArrayList<>();
        String[] dotTypes = {"default", "minimal", "mojang", "bowtie"};
        for (String dotType : dotTypes) {
            Identifier testId = Identifier.of(Constants.MOD_ID, String.format("textures/sprites/hud/player_dots/%s_0.png", dotType));
            if (resourceManager.getResource(testId).isPresent()) {
                dots.add(dotType);
            }
        }
        return dots;
    }

    public static List<String> getIconBorderStyles(ResourceManager resourceManager) {
        return checkExistence(resourceManager, new String[]{"rounded", "squared"}, style -> String.format("textures/sprites/hud/player_dots_outlines/%s/default_0.png", style));
    }

    public static List<String> getNameplateBorderStyles(ResourceManager resourceManager) {
        return checkExistence(resourceManager, new String[]{"rounded", "squared"}, style -> String.format("textures/sprites/hud/tags/%s/default.png", style));
    }

    public static List<String> getArrowTypes(ResourceManager resourceManager) {
        return checkExistence(resourceManager, new String[]{"default", "mojang"}, arrow -> String.format("textures/sprites/hud/arrows/%s.png", arrow));
    }

    public static List<String> getDeathMarkerTypes(ResourceManager resourceManager) {
        return checkExistence(resourceManager, new String[]{"default", "minimal"}, marker -> String.format("textures/sprites/hud/death_markers_dots/%s.png", marker));
    }

    private static List<String> checkExistence(ResourceManager resourceManager, String[] types, java.util.function.Function<String, String> pathBuilder) {
        List<String> result = new ArrayList<>();
        for (String type : types) {
            Identifier testId = Identifier.of(Constants.MOD_ID, pathBuilder.apply(type));
            if (resourceManager.getResource(testId).isPresent()) {
                result.add(type);
            }
        }
        return result;
    }

    public static Dimension getTextureDimensions(Identifier id) {
        return dimensionCache.computeIfAbsent(id, key -> {
            try {
                ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
                Optional<Resource> resourceOpt = resourceManager.getResource(key);
                if (resourceOpt.isPresent()) {
                    try (InputStream stream = resourceOpt.get().getInputStream(); NativeImage image = NativeImage.read(stream)) {
                        return new Dimension(image.getWidth(), image.getHeight());
                    }
                }
            } catch (Exception e) {
                Constants.LOGGER.warn("Could not read dimensions for texture: {}", key, e);
            }
            return new Dimension(Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE);
        });
    }
}
