package net.bichal.bplb.client.render;

import net.bichal.bplb.util.Constants;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new HashMap<>();
    private static ResourceLocation getTexture(String... parts) {
        String path = String.format("textures/sprites/hud/%s/%s.png", "player_dots", String.join("/", parts));
        return TEXTURE_CACHE.computeIfAbsent(String.join("_", parts), k -> ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path));
    }

    public static ResourceLocation getPlayerDotTexture(String dotId, int textureIndex) {
        String dotPath = "bowtie".equals(dotId) ? "bowtie_" + textureIndex : dotId + "_" + textureIndex;
        return getTexture(dotPath);
    }

    public static ResourceLocation getPlayerDotOutlineTexture(String dotId, String borderStyle, String borderType, int textureIndex) {
        String key = "outline_" + dotId + "_" + borderStyle + "_" + borderType + "_" + textureIndex;
        return TEXTURE_CACHE.computeIfAbsent(key, k -> {
            String path = "bowtie".equals(dotId) ? "textures/sprites/hud/player_dots_outlines/bowtie_" + textureIndex + ".png" : "textures/sprites/hud/player_dots_outlines/" + borderStyle + "/" + borderType + "_" + textureIndex + ".png";
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
        });
    }

    public static ResourceLocation getArrowTexture(String arrowType) {
        return TEXTURE_CACHE.computeIfAbsent("arrow_" + arrowType, k -> ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, String.format("textures/sprites/hud/arrows/%s.png", arrowType)));
    }

    public static ResourceLocation getDeathMarkerTexture(String markerType) {
        return TEXTURE_CACHE.computeIfAbsent("marker_" + markerType, k -> ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, String.format("textures/sprites/hud/death_markers_dots/%s.png", markerType)));
    }

    public static ResourceLocation getDeathMarkerOutlineTexture(String markerType, String borderStyle, String borderType) {
        return TEXTURE_CACHE.computeIfAbsent("marker_outline_" + markerType + "_" + borderStyle + "_" + borderType, k ->
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, String.format("textures/sprites/hud/death_markers_dots_outlines/%s.png", borderType))
        );
    }

    public static ResourceLocation getNameplateTexture(String borderStyle) {
        return TEXTURE_CACHE.computeIfAbsent("nameplate_" + borderStyle, k -> ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, String.format("textures/sprites/hud/tags/%s/default.png", borderStyle)));
    }

    public static int getTextureIndexFromDistance(double distance) {
        if (distance <= 64) return 0;
        if (distance <= 128) return 1;
        if (distance <= 256) return 2;
        return 3;
    }
}
