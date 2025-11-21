package net.bichal.bplb.gui.widget;

import net.bichal.bplb.client.render.RenderAddons;
import net.bichal.bplb.gui.Config;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.UUID;

public final class DotPreviewWidget {
    private static final int DOT_SIZE = 9;

    public static void render(DrawContext context, int x, int y, Config.PlayerAppearance appearance, String playerName, UUID uuid) {
        MinecraftClient client = MinecraftClient.getInstance();

        String dotType = appearance != null && appearance.dotType != null ?
                appearance.dotType : Constants.CONFIG.getDotType();
        String borderStyle = appearance != null && appearance.iconBorderStyle != null ?
                appearance.iconBorderStyle : Constants.CONFIG.getIconBorderStyle();
        int color = appearance != null && appearance.color != null ?
                appearance.color : net.bichal.bichalutils.util.ColorUtil.generateColorFromUUID(uuid);

        RenderAddons.renderPlayerIcon(context, playerName, uuid, 0, x, y, false, Constants.CONFIG, 1.0f, null);
    }
}