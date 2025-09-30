package net.bichal.bplb.client.render;

import net.bichal.bplb.util.Constants;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private static final Map<String, Identifier> TEXTURE_CACHE = new HashMap<>();

    private static Identifier getTexture(String... parts) {
        String path = String.format("textures/sprites/hud/%s/%s.png", "player_dots", String.join("/", parts));
        return TEXTURE_CACHE.computeIfAbsent(String.join("_", parts),
                k -> Identifier.of(Constants.MOD_ID, path));
    }

    public static Identifier getPlayerDotTexture(String dotId, int textureIndex) {
        return getTexture(dotId.equals("bowtie") ?
                String.format("bowtie_%d", textureIndex) : String.format("%s_%d", dotId, textureIndex));
    }

    public static Identifier getPlayerDotOutlineTexture(String dotId, String borderStyle, String borderType, int textureIndex) {
        String key = "outline_" + dotId + "_" + borderStyle + "_" + borderType + "_" + textureIndex;
        return TEXTURE_CACHE.computeIfAbsent(key, k -> {
            String path = "bowtie".equals(dotId)
                    ? String.format("textures/sprites/hud/player_dots_outlines/bowtie_%d.png", textureIndex)
                    : String.format("textures/sprites/hud/player_dots_outlines/%s/%s_%d.png", borderStyle, borderType, textureIndex);
            return Identifier.of(Constants.MOD_ID, path);
        });
    }

    public static Identifier getArrowTexture(String arrowType) {
        return TEXTURE_CACHE.computeIfAbsent("arrow_" + arrowType,
                k -> Identifier.of(Constants.MOD_ID, String.format("textures/sprites/hud/arrows/%s.png", arrowType)));
    }

    public static Identifier getDeathMarkerTexture(String markerType) {
        return TEXTURE_CACHE.computeIfAbsent("marker_" + markerType,
                k -> Identifier.of(Constants.MOD_ID, String.format("textures/sprites/hud/death_markers_dots/%s.png", markerType)));
    }

    public static Identifier getDeathMarkerOutlineTexture(String markerType, String borderStyle, String borderType) {
        return TEXTURE_CACHE.computeIfAbsent("marker_outline_" + markerType + "_" + borderStyle + "_" + borderType, k ->
                Identifier.of(Constants.MOD_ID, String.format("textures/sprites/hud/death_markers_dots_outlines/%s.png", borderType))
        );
    }

    public static Identifier getNameplateTexture(String borderStyle) {
        return TEXTURE_CACHE.computeIfAbsent("nameplate_" + borderStyle,
                k -> Identifier.of(Constants.MOD_ID, String.format("textures/sprites/hud/tags/%s/default.png", borderStyle)));
    }

    public static int getTextureIndexFromDistance(double distance) {
        if (distance <= 64) return 0;
        if (distance <= 128) return 1;
        if (distance <= 256) return 2;
        return 3;
    }
}
