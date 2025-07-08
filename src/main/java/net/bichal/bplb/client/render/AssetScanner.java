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

    public static List<String> getSpriteNames(ResourceManager resourceManager, String subfolder) {
        List<String> names = new ArrayList<>();
        Identifier root = Identifier.of(Constants.MOD_ID, "textures/sprites/hud/" + subfolder);

        Map<Identifier, Resource> resources = resourceManager.findResources(root.getPath(), id -> id.getPath().endsWith(".png"));

        for (Identifier id : resources.keySet()) {
            String path = id.getPath();
            String name = path.substring(root.getPath().length() + 1, path.length() - 4);
            names.add(name);
        }
        names.sort(Comparator.naturalOrder());
        return names;
    }

    public static Dimension getTextureDimensions(Identifier id) {
        if (dimensionCache.containsKey(id)) {
            return dimensionCache.get(id);
        }
        try {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
            Optional<Resource> resourceOpt = resourceManager.getResource(id);

            if (resourceOpt.isPresent()) {
                Dimension dim;
                try (InputStream stream = resourceOpt.get().getInputStream(); NativeImage image = NativeImage.read(stream)) {
                    dim = new Dimension(image.getWidth(), image.getHeight());
                    dimensionCache.put(id, dim);
                }
                return dim;
            }
        } catch (Exception e) {
            Constants.LOGGER.warn("Could not read dimensions for texture: {}", id, e);
        }
        return new Dimension(16, 16);
    }
}
