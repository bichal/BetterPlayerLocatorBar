package net.bichal.bplb.config;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.bichal.bplb.client.Client;
import net.bichal.bplb.client.Hud;
import net.bichal.bplb.client.render.AssetScanner;
import net.bichal.bplb.config.widget.CustomButtonWidget;
import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.config.widget.entries.CycleOptionEntry;
import net.bichal.bplb.config.widget.entries.IntegerSliderOptionEntry;
import net.bichal.bplb.config.widget.entries.SectionHeaderEntry;
import net.bichal.bplb.config.widget.entries.ToggleOptionEntry;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class PlayerAppearanceConfigScreen extends Screen {
    private final Screen parent;
    private final PlayerAppearance appearance;
    private final Consumer<PlayerAppearance> onSave;

    private ScrollableListWidget list;
    private int r, g, b;

    public PlayerAppearanceConfigScreen(Screen parent, String playerName, PlayerAppearance appearance, Consumer<PlayerAppearance> onSave) {
        super(Text.translatable(Constants.CONFIG_KEY_PREFIX + "player_appearance.title", playerName));
        this.parent = parent;
        this.appearance = appearance;
        this.onSave = onSave;

        if (this.appearance.color != null) {
            this.r = (this.appearance.color >> 16) & 0xFF;
            this.g = (this.appearance.color >> 8) & 0xFF;
            this.b = this.appearance.color & 0xFF;
        } else {
            this.r = 255;
            this.g = 255;
            this.b = 255;
        }
    }

    @Override protected void init() {
        super.init();

        int maxUiWidth = 900;
        int uiWidth = Math.min(this.width - 40, maxUiWidth);
        int xOffset = (this.width - uiWidth) / 2;
        int previewSize = 144;
        int previewPadding = 20;
        int listWidth = uiWidth - previewSize - previewPadding;

        this.list = new ScrollableListWidget(this.client, listWidth, 32, this.height - 32, 24);
        this.list.setX(xOffset);

        List<String> borderStyles = Lists.newArrayList("Default");
        borderStyles.addAll(List.of(Constants.BORDER_STYLE_ROUNDED, Constants.BORDER_STYLE_SQUARED));

        List<String> dotStyles = Lists.newArrayList("Default");
        dotStyles.addAll(Client.availableDots);

        List<String> arrowStyles = Lists.newArrayList("Default");
        arrowStyles.addAll(Client.availableArrows);

        this.list.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.player_appearance.section.style")));
        this.list.addPublicEntry(new CycleOptionEntry<>(this.client, "player_appearance.border_style", this.appearance.borderId == null ? "Default" : this.appearance.borderId, borderStyles, Text::literal, (val) -> this.appearance.borderId = val.equals("Default") ? null : val, () -> {
        }));
        this.list.addPublicEntry(new CycleOptionEntry<>(this.client, "player_appearance.dot_style", this.appearance.dotId == null ? "Default" : this.appearance.dotId, dotStyles, Text::literal, (val) -> this.appearance.dotId = val.equals("Default") ? null : val, () -> {
        }));
        this.list.addPublicEntry(new CycleOptionEntry<>(this.client, "player_appearance.arrow_style", this.appearance.arrowId == null ? "Default" : this.appearance.arrowId, arrowStyles, Text::literal, (val) -> this.appearance.arrowId = val.equals("Default") ? null : val, () -> {
        }));

        this.list.addPublicEntry(new SectionHeaderEntry(this.client, Text.translatable("bplb.config.player_appearance.section.color")));
        this.list.addPublicEntry(new ToggleOptionEntry(this.client, "player_appearance.use_custom_color", this.appearance.color != null, this::toggleCustomColor, () -> {
        }));
        this.list.addPublicEntry(new IntegerSliderOptionEntry(this.client, "R", r, 0, 255, (val) -> {
            r = val;
            updateColor();
        }, () -> {
        }));
        this.list.addPublicEntry(new IntegerSliderOptionEntry(this.client, "G", g, 0, 255, (val) -> {
            g = val;
            updateColor();
        }, () -> {
        }));
        this.list.addPublicEntry(new IntegerSliderOptionEntry(this.client, "B", b, 0, 255, (val) -> {
            b = val;
            updateColor();
        }, () -> {
        }));

        this.addDrawableChild(this.list);
        this.addDrawableChild(new CustomButtonWidget.Builder(ScreenTexts.DONE, button -> this.close()).dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    private void toggleCustomColor(boolean enabled) {
        if (enabled) {
            this.appearance.color = (0xFF << 24) | (r << 16) | (g << 8) | b;
        } else {
            this.appearance.color = null;
        }
        if (this.client != null)
            this.client.setScreen(new PlayerAppearanceConfigScreen(this.parent, this.title.getString(), this.appearance, this.onSave));
    }

    private void updateColor() {
        if (this.appearance.color != null) {
            this.appearance.color = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        this.list.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

        renderPreview(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderPreview(DrawContext context) {
        int maxUiWidth = 900;
        int uiWidth = Math.min(this.width - 40, maxUiWidth);
        int xOffset = (this.width - uiWidth) / 2;
        int previewSize = 144;
        int previewPadding = 20;
        int listWidth = uiWidth - previewSize - previewPadding;

        int previewX = xOffset + listWidth + previewPadding;
        int previewY = this.height / 2 - previewSize / 2;

        Text previewLabel = Text.translatable("bplb.config.player_appearance.preview").formatted(Formatting.BOLD, Formatting.GRAY);
        context.drawTextWithShadow(this.textRenderer, previewLabel, previewX + (previewSize - this.textRenderer.getWidth(previewLabel)) / 2, previewY - 15, 0xFFFFFF);

        String dotId = this.appearance.dotId != null ? this.appearance.dotId : Config.getInstance().getDotType();
        Identifier texture = Identifier.of(Constants.MOD_ID, "textures/sprites/hud/dots/" + dotId + ".png");
        Dimension dim = AssetScanner.getTextureDimensions(texture);

        int color = this.appearance.color != null ? this.appearance.color : 0xFF808080;

        context.getMatrices().push();
        context.getMatrices().translate(previewX, previewY, 0);

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        int thickness = previewSize / 5;

        RenderSystem.setShaderColor(r, g, b, 1.0f);
        context.drawTexture(texture, thickness, thickness, previewSize - (thickness * 2), previewSize - (thickness * 2), 0, 0, dim.width, dim.height, dim.width, dim.height);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        String borderId = this.appearance.borderId != null ? this.appearance.borderId : Config.getInstance().getIconBorderStyle();

        context.getMatrices().pop();
    }

    @Override public void close() {
        this.onSave.accept(this.appearance);
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
