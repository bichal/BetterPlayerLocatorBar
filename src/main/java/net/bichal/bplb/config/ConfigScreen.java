package net.bichal.bplb.config;

import net.bichal.bplb.client.Client;
import net.bichal.bplb.client.render.RenderAddons;
import net.bichal.bplb.client.render.RenderUtils;
import net.bichal.bplb.client.render.TextureAnimator;
import net.bichal.bplb.client.render.TextureManager;
import net.bichal.bplb.config.ui.Row;
import net.bichal.bplb.config.ui.Rows;
import net.bichal.bplb.config.ui.Theme;
import net.bichal.bplb.util.ColorUtils;
import net.bichal.bplb.util.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.bichal.bplb.client.render.RenderAddons.getUuidFromCache;
import static net.bichal.bplb.util.ColorUtils.generateColorFromUUID;

/**
 * The settings screen: a category sidebar, a searchable option list and a live preview.
 * Edits go to a working copy of the config and only reach the real one on Apply or Done.
 */
public class ConfigScreen extends Screen {
    private enum Category {
        GENERAL("general"), ICONS("icon"), MARKERS("markers"), NAME_TAGS("player_name"), FADING("fading"), PLAYERS("player_appearance");

        final String key;

        Category(String key) {
            this.key = key;
        }

        Component title() {
            return Component.translatable(Constants.CONFIG_KEY_PREFIX + "section." + key);
        }
    }

    private static final int MARGIN = 12;
    private static final int PREVIEW_SIZE = 40;
    private static final int PREVIEW_PANEL_WIDTH = 84;
    private static final int TAB_HEIGHT = 20;

    private final Screen parent;
    private final Config workingConfig;
    private final List<Row> rows = new ArrayList<>();
    private Category category = Category.GENERAL;
    private String searchQuery = "";
    private EditBox searchBox;
    private Button applyButton;
    private Button doneButton;
    private TextureAnimator previewArrowAnimator;
    private boolean hasChanges;
    private double scroll;
    private boolean draggingScrollbar;
    private int listLeft, listRight, listTop, listBottom;
    private int sidebarLeft, sidebarWidth;
    private int previewLeft;
    private boolean showPreview;

    public ConfigScreen(Screen parent) {
        super(Component.translatable(Constants.CONFIG_KEY_PREFIX + "title"));
        this.parent = parent;
        this.workingConfig = new Config();
        Config.copy(Config.getInstance(), this.workingConfig);
    }

    @Override
    protected void init() {
        sidebarLeft = MARGIN;
        sidebarWidth = this.width < 420 ? 74 : 100;
        showPreview = this.width >= 440;
        listLeft = sidebarLeft + sidebarWidth + 8;
        previewLeft = this.width - MARGIN - PREVIEW_PANEL_WIDTH;
        listRight = showPreview ? previewLeft - 8 : this.width - MARGIN;
        listTop = 52;
        listBottom = this.height - 36;

        String previous = searchBox != null ? searchBox.getValue() : "";
        searchBox = new EditBox(this.font, listLeft, 28, Math.min(220, listRight - listLeft), 18, Component.translatable("bplb.config.search"));
        searchBox.setHint(Component.translatable("bplb.config.search").withStyle(ChatFormatting.GRAY));
        searchBox.setValue(previous);
        searchBox.setResponder(text -> {
            searchQuery = text.trim().toLowerCase(Locale.ROOT);
            scroll = 0;
            rebuildRows();
        });
        this.addRenderableWidget(searchBox);

        int buttonWidth = Constants.CONFIG_BUTTON_WIDTH;
        int gap = Constants.CONFIG_BUTTON_SPACING;
        int startX = this.width / 2 - (buttonWidth * 3 + gap * 2) / 2;
        int y = this.height - 28;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> this.onClose()).bounds(startX, y, buttonWidth, 20).build());
        applyButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.bplb.config.apply"), b -> applyChanges()).bounds(startX + buttonWidth + gap, y, buttonWidth, 20).build());
        doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
            if (hasChanges) applyChanges();
            this.onClose();
        }).bounds(startX + (buttonWidth + gap) * 2, y, buttonWidth, 20).build());

        rebuildRows();
        updateButtons();
    }

    // ------------------------------------------------------------------ rows

    private Supplier<String> query() {
        return () -> searchQuery;
    }

    private Component label(String key) {
        return Component.translatable(Constants.CONFIG_KEY_PREFIX + key);
    }

    private Component assetName(String id, String group) {
        String key = "bplb.config.asset." + group + "." + id;
        Component translated = Component.translatable(key);
        return translated.getString().equals(key) ? Component.literal(id) : translated;
    }

    private void changed() {
        hasChanges = true;
        updateButtons();
    }

    private Rows.Toggle toggle(String key, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
        return new Rows.Toggle(key, label(key), query(), getter, v -> {
            setter.accept(v);
            changed();
        });
    }

    private Rows.Slider intSlider(String key, int min, int max, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter) {
        return Rows.Slider.ofInt(key, label(key), query(), min, max, getter, v -> {
            setter.accept(v);
            changed();
        });
    }

    private Rows.Slider floatSlider(String key, java.util.function.DoubleSupplier min, java.util.function.DoubleSupplier max, java.util.function.DoubleSupplier getter, java.util.function.DoubleConsumer setter) {
        return Rows.Slider.ofFloat(key, label(key), query(), min, max, getter, v -> {
            setter.accept(v);
            changed();
        });
    }

    private <T> Rows.Cycle<T> cycle(String key, Component name, List<T> options, Function<T, Component> text, Supplier<T> getter, java.util.function.Consumer<T> setter, boolean active) {
        return new Rows.Cycle<>(key, name, query(), options, text, getter, v -> {
            setter.accept(v);
            changed();
        }, () -> active);
    }

    private List<Row> rowsFor(Category c) {
        boolean bowtie = workingConfig.getDotType().equals("bowtie");
        List<String> borderStyles = Arrays.asList(Constants.BORDER_STYLE_ROUNDED, Constants.BORDER_STYLE_SQUARED);
        List<String> borderTypes = Arrays.asList("default", "minimal");
        List<String> heightModes = Arrays.asList("player", "camera");
        List<String> markerTypes = Arrays.asList("default", "minimal");
        Function<String, Component> borderStyleText = v -> label("border_style." + v.toLowerCase(Locale.ROOT));
        Function<String, Component> borderTypeText = v -> label("border_type." + v.toLowerCase(Locale.ROOT));
        Function<String, Component> heightModeText = v -> label("height_difference_mode." + v.toLowerCase(Locale.ROOT));
        List<Row> out = new ArrayList<>();
        Config w = workingConfig;

        switch (c) {
            case GENERAL -> {
                out.add(toggle("modEnabled", w::isModEnabled, w::setModEnabled));
                out.add(toggle("apply_hotbar_offset", w::isApplyHotbarOffset, w::setApplyHotbarOffset));
                out.add(toggle("always_show_player_heads", w::isAlwaysShowPlayerHeads, w::setAlwaysShowPlayerHeads));
                out.add(toggle("always_show_player_names", w::isAlwaysShowPlayerNames, w::setAlwaysShowPlayerNames));
                out.add(intSlider("max_visible_icons", 1, 200, w::getMaxVisibleIcons, w::setMaxVisibleIcons));
                out.add(floatSlider("lerp_speed", () -> 0.1, () -> 1.0, w::getLerpSpeed, v -> w.setLerpSpeed((float) v)));
            }
            case ICONS -> {
                out.add(intSlider("icon_size", 1, 4, w::getIconSize, w::setIconSize));
                List<String> dots = Client.availableDots.stream().filter(d -> !d.equals("bowtie")).toList();
                out.add(cycle("dot_type", label("dot_type"), dots.isEmpty() ? List.of(w.getDotType()) : dots, id -> assetName(id, "dot"), w::getDotType, v -> {
                    w.setDotType(v);
                    rebuildRows();
                }, true));
                out.add(cycle("icon_border_style", label("icon_border_style"), borderStyles, borderStyleText, w::getIconBorderStyle, w::setIconBorderStyle, !bowtie));
                out.add(cycle("icon_border_type", label("icon_border_type"), borderTypes, borderTypeText, w::getIconBorderType, w::setIconBorderType, !bowtie));
                out.add(toggle("inherit_border_color", w::isInheritBorderColor, w::setInheritBorderColor));
                out.add(cycle("height_difference_mode", label("height_difference_mode"), heightModes, heightModeText, w::getHeightDifferenceMode, w::setHeightDifferenceMode, true));
                List<String> arrows = Client.availableArrows.isEmpty() ? List.of("default") : Client.availableArrows;
                out.add(cycle("arrow_type", label("arrow_type"), arrows, id -> assetName(id, "arrow"), w::getArrowType, v -> {
                    w.setArrowType(v);
                    previewArrowAnimator = null;
                }, true));
                out.add(intSlider("vertical_padding", 0, 4, w::getVerticalPadding, w::setVerticalPadding));
                out.add(toggle("adjust_to_fov", w::isAdjustToFov, w::setAdjustToFov));
                out.add(floatSlider("fov_multiplier", () -> 0.5, () -> 2.0, w::getFovMultiplier, v -> w.setFovMultiplier((float) v)));
            }
            case MARKERS -> {
                out.add(cycle("death_marker_type", label("death_marker_type"), markerTypes, id -> assetName(id, "marker"), w::getDeathMarkerType, w::setDeathMarkerType, true));
                out.add(cycle("death_marker_border_type", label("death_marker_border_type"), borderTypes, borderTypeText, w::getDeathMarkerBorderType, w::setDeathMarkerBorderType, true));
                out.add(toggle("death_marker_inherit_border_color", w::isDeathMarkerInheritBorderColor, w::setDeathMarkerInheritBorderColor));
            }
            case NAME_TAGS -> {
                out.add(floatSlider("nameplate_scale", () -> 0.5, () -> 1.5, w::getNameplateScale, v -> w.setNameplateScale((float) v)));
                out.add(cycle("name_border_style", label("name_border_style"), borderStyles, borderStyleText, w::getNameBorderStyle, w::setNameBorderStyle, true));
            }
            case FADING -> {
                out.add(intSlider("fade_start_distance", 5, 9995, w::getFadeStartDistance, w::setFadeStartDistance));
                out.add(intSlider("fade_end_distance", 10, 10000, w::getFadeEndDistance, w::setFadeEndDistance));
                out.add(floatSlider("fade_alpha_max", w::getFadeAlphaMin, () -> 1.0, w::getFadeAlphaMax, v -> w.setFadeAlphaMax((float) v)));
                out.add(floatSlider("fade_alpha_min", () -> 0.0, w::getFadeAlphaMax, w::getFadeAlphaMin, v -> w.setFadeAlphaMin((float) v)));
            }
            case PLAYERS -> addPlayerRows(out, borderStyles, borderTypes, borderStyleText, borderTypeText);
        }
        return out;
    }

    private void addPlayerRows(List<Row> out, List<String> borderStyles, List<String> borderTypes, Function<String, Component> borderStyleText, Function<String, Component> borderTypeText) {
        Config w = workingConfig;
        out.add(new Rows.AddPlayer(Component.translatable("bplb.config.player_appearance.add_player"),
                name -> !w.getPlayerConfigs().containsKey(name),
                name -> {
                    w.getPlayerConfigs().put(name, new Config.PlayerAppearance());
                    w.setPlayerExpanded(name, true);
                    changed();
                    rebuildRows();
                }));
        w.getPlayerConfigs().keySet().stream().sorted().forEach(name -> {
            if (!searchQuery.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(searchQuery)) return;
            out.add(new Rows.PlayerHeader(name, query(), () -> w.isPlayerExpanded(name), () -> {
                w.setPlayerExpanded(name, !w.isPlayerExpanded(name));
                rebuildRows();
            }, () -> {
                w.getPlayerConfigs().remove(name);
                changed();
                rebuildRows();
            }));
            Config.PlayerAppearance pc = w.getPlayerConfigs().get(name);
            if (pc == null || !w.isPlayerExpanded(name)) return;
            UUID uuid = getUuidFromCache(name);
            int defaultColor = uuid != null ? generateColorFromUUID(uuid) : 0xFF808080;
            out.add(new Rows.ColorField("player.color", label("player_appearance.color"), query(), pc.color != null ? pc.color : defaultColor, v -> pc.color = v, this::changed));
            List<String> dots = Client.availableDots.stream().filter(d -> !d.equals("bowtie")).toList();
            out.add(cycle("player.dot", label("player_appearance.dot_type"), dots.isEmpty() ? List.of("default") : dots, id -> assetName(id, "dot"), () -> pc.dotType != null ? pc.dotType : "default", v -> pc.dotType = v, true));
            out.add(cycle("player.border", label("player_appearance.icon_border_style"), borderStyles, borderStyleText, () -> pc.iconBorderStyle != null ? pc.iconBorderStyle : "rounded", v -> pc.iconBorderStyle = v, true));
            out.add(cycle("player.border_type", label("player_appearance.icon_border_type"), borderTypes, borderTypeText, () -> pc.iconBorderType != null ? pc.iconBorderType : "default", v -> pc.iconBorderType = v, true));
            List<String> arrows = Client.availableArrows.isEmpty() ? List.of("default") : Client.availableArrows;
            out.add(cycle("player.arrow", label("player_appearance.arrow_type"), arrows, id -> assetName(id, "arrow"), () -> pc.arrowType != null ? pc.arrowType : "default", v -> pc.arrowType = v, true));
        });
    }

    private void rebuildRows() {
        for (Row row : rows) row.blur();
        rows.clear();
        if (searchQuery.isEmpty()) {
            rows.addAll(rowsFor(category));
            if (category == Category.PLAYERS || category == Category.GENERAL) {
                rows.add(new Rows.Action(label("reset_settings").copy(), net.minecraft.client.gui.screens.Screen::hasShiftDown, this::confirmReset));
            }
        } else {
            for (Category c : Category.values()) {
                List<Row> matching = new ArrayList<>();
                for (Row row : rowsFor(c)) {
                    String key = row.key();
                    if (key == null || key.startsWith("player")) {
                        if (c == Category.PLAYERS && key != null && !key.equals("player")) matching.add(row);
                        continue;
                    }
                    if (label(key).getString().toLowerCase(Locale.ROOT).contains(searchQuery)) matching.add(row);
                }
                if (!matching.isEmpty()) {
                    rows.add(new Rows.Section(c.title()));
                    rows.addAll(matching);
                }
            }
        }
        clampScroll();
    }

    private void confirmReset() {
        this.minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                workingConfig.resetToDefaults();
                changed();
                rebuildRows();
            }
            this.minecraft.setScreen(this);
        }, Component.translatable("bplb.config.reset_confirm.title"), Component.translatable("bplb.config.reset_confirm.message")));
    }

    // ---------------------------------------------------------------- layout

    private int contentHeight() {
        int total = 0;
        for (Row row : rows) total += row.height();
        return total;
    }

    private void clampScroll() {
        double max = Math.max(0, contentHeight() - (listBottom - listTop));
        scroll = Mth.clamp(scroll, 0, max);
    }

    private int rowX() {
        return listLeft;
    }

    private int rowWidth() {
        return listRight - listLeft - 8;
    }

    /** Index of the row under the pointer, or -1. */
    private int rowAt(double mouseX, double mouseY) {
        if (mouseX < listLeft || mouseX >= listRight || mouseY < listTop || mouseY >= listBottom) return -1;
        int y = listTop - (int) scroll;
        for (int i = 0; i < rows.size(); i++) {
            int h = rows.get(i).height();
            if (mouseY >= y && mouseY < y + h) return i;
            y += h;
        }
        return -1;
    }

    private int rowTop(int index) {
        int y = listTop - (int) scroll;
        for (int i = 0; i < index; i++) y += rows.get(i).height();
        return y;
    }

    // ------------------------------------------------------------- rendering

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // Screen.render draws the (blurred) background and the widgets exactly once; drawing the
        // background here as well would blur the screen twice.
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(this.font, this.title, this.width / 2, 10, Theme.TEXT);

        renderSidebar(g, mouseX, mouseY);
        renderList(g, mouseX, mouseY);
        if (showPreview) renderPreviewPanel(g);

        //? if waypoints {
        /*Component mode = Component.translatable("bplb.config.waypoint_mode").withStyle(ChatFormatting.GREEN);
        *///?} else {
        Component mode = Client.isLocalMode()
                ? Component.translatable("bplb.config.local_mode_warning").withStyle(ChatFormatting.RED)
                : Component.translatable("bplb.config.remote_mode").withStyle(ChatFormatting.GREEN);
        //?}
        g.drawString(this.font, mode, this.width - this.font.width(mode) - 5, this.height - 12, Theme.TEXT, false);
    }

    private void renderSidebar(GuiGraphics g, int mouseX, int mouseY) {
        g.fill(sidebarLeft, listTop, sidebarLeft + sidebarWidth, listBottom, Theme.PANEL);
        int y = listTop + 4;
        for (Category c : Category.values()) {
            boolean selected = searchQuery.isEmpty() && c == category;
            boolean over = mouseX >= sidebarLeft && mouseX < sidebarLeft + sidebarWidth && mouseY >= y && mouseY < y + TAB_HEIGHT;
            if (selected) {
                g.fill(sidebarLeft, y, sidebarLeft + sidebarWidth, y + TAB_HEIGHT, Theme.ROW_HOVER);
                g.fill(sidebarLeft, y, sidebarLeft + 2, y + TAB_HEIGHT, Theme.ACCENT);
            } else if (over) {
                g.fill(sidebarLeft, y, sidebarLeft + sidebarWidth, y + TAB_HEIGHT, Theme.ROW);
            }
            Component title = c.title();
            g.drawString(this.font, this.font.plainSubstrByWidth(title.getString(), sidebarWidth - 14), sidebarLeft + 8, y + 6, selected ? Theme.TEXT : Theme.TEXT_DIM, false);
            y += TAB_HEIGHT;
        }
    }

    private void renderList(GuiGraphics g, int mouseX, int mouseY) {
        g.fill(listLeft - 2, listTop, listRight, listBottom, Theme.PANEL);
        g.enableScissor(listLeft, listTop, listRight, listBottom);
        int hovered = rowAt(mouseX, mouseY);
        int y = listTop - (int) scroll;
        int x = rowX();
        int w = rowWidth();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            int h = row.height();
            if (y + h >= listTop && y <= listBottom) {
                row.render(g, x, y, w, mouseX, mouseY, i == hovered);
            }
            y += h;
        }
        if (rows.isEmpty()) {
            Component empty = Component.translatable("bplb.config.no_results");
            g.drawCenteredString(this.font, empty, (listLeft + listRight) / 2, listTop + 20, Theme.TEXT_DIM);
        }
        g.disableScissor();

        int visible = listBottom - listTop;
        int total = contentHeight();
        if (total > visible) {
            int barX = listRight - 5;
            int barH = Math.max(16, visible * visible / total);
            int barY = listTop + (int) ((visible - barH) * (scroll / (total - visible)));
            g.fill(barX, listTop, barX + 3, listBottom, Theme.TRACK);
            g.fill(barX, barY, barX + 3, barY + barH, draggingScrollbar ? Theme.ACCENT : Theme.TEXT_DIM);
        }
    }

    private void renderPreviewPanel(GuiGraphics g) {
        g.fill(previewLeft, listTop, previewLeft + PREVIEW_PANEL_WIDTH, listBottom, Theme.PANEL);
        g.drawCenteredString(this.font, Component.translatable("bplb.config.preview"), previewLeft + PREVIEW_PANEL_WIDTH / 2, listTop + 6, Theme.TEXT_DIM);

        int centerX = previewLeft + PREVIEW_PANEL_WIDTH / 2;
        int padding = workingConfig.getVerticalPadding() * 3;
        int available = listBottom - listTop - 24;
        int blockHeight = PREVIEW_SIZE * 4 + padding * 2;
        int top = listTop + 20 + Math.max(0, (available - blockHeight) / 2);

        renderArrowPreview(g, centerX, top + PREVIEW_SIZE / 2, true);
        renderIconPreview(g, centerX, top + PREVIEW_SIZE + padding + PREVIEW_SIZE / 2);
        renderArrowPreview(g, centerX, top + PREVIEW_SIZE * 2 + padding * 2 + PREVIEW_SIZE / 2, false);
        renderDeathMarkerPreview(g, centerX, top + PREVIEW_SIZE * 3 + padding * 2 + PREVIEW_SIZE / 2);
    }

    private void renderIconPreview(GuiGraphics g, int centerX, int centerY) {
        RenderUtils.withMatrixPush(g, centerX - PREVIEW_SIZE / 2f, centerY - PREVIEW_SIZE / 2f, () -> {
            int textureIndex = workingConfig.getIconSize() == 4 ? 0 : 4 - workingConfig.getIconSize();
            String dotId = workingConfig.getDotType();
            int color = 0xFFFFFFFF;
            ResourceLocation dot = TextureManager.getPlayerDotTexture(dotId, textureIndex);
            ResourceLocation outline = TextureManager.getPlayerDotOutlineTexture(dotId, workingConfig.getIconBorderStyle(), workingConfig.getIconBorderType(), textureIndex);
            int borderColor = workingConfig.isInheritBorderColor() ? ColorUtils.darkerColoring(color) : Constants.BLACK_COLOR;
            RenderUtils.renderTintedTexture(g, outline, 0, 0, PREVIEW_SIZE, PREVIEW_SIZE, borderColor, 1.0f);
            RenderUtils.renderTintedTexture(g, dot, 0, 0, PREVIEW_SIZE, PREVIEW_SIZE, color, 1.0f);
        });
    }

    private void renderArrowPreview(GuiGraphics g, int centerX, int centerY, boolean up) {
        RenderUtils.withMatrixPush(g, centerX, centerY, () -> {
            if (previewArrowAnimator == null) previewArrowAnimator = new TextureAnimator(10, 4);
            ResourceLocation texture = TextureManager.getArrowTexture(workingConfig.getArrowType());
            int frame = previewArrowAnimator.getCurrentFrame();
            float u = up ? 0 : Constants.ICON_BASE_SIZE;
            float v = frame * Constants.ICON_BASE_SIZE;
            RenderUtils.renderTexture(g, texture, -PREVIEW_SIZE / 2, -PREVIEW_SIZE / 2, PREVIEW_SIZE, PREVIEW_SIZE, u, v, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE * 2, Constants.ICON_BASE_SIZE * 2, 0xFFFFFFFF, 1.0f);
        });
    }

    private void renderDeathMarkerPreview(GuiGraphics g, int centerX, int centerY) {
        RenderUtils.withMatrixPush(g, centerX - PREVIEW_SIZE / 2f, centerY - PREVIEW_SIZE / 2f, () ->
                RenderAddons.renderDeathMarker(g, 0, 0, PREVIEW_SIZE, 1.0f, workingConfig));
    }

    // ----------------------------------------------------------------- input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= sidebarLeft && mouseX < sidebarLeft + sidebarWidth && mouseY >= listTop && mouseY < listBottom) {
            int index = (int) ((mouseY - listTop - 4) / TAB_HEIGHT);
            Category[] all = Category.values();
            if (index >= 0 && index < all.length) {
                category = all[index];
                searchBox.setValue("");
                searchQuery = "";
                scroll = 0;
                rebuildRows();
                return true;
            }
        }
        if (button == 0 && showScrollbar() && mouseX >= listRight - 7 && mouseX < listRight && mouseY >= listTop && mouseY < listBottom) {
            draggingScrollbar = true;
            dragScrollbar(mouseY);
            return true;
        }
        int hit = rowAt(mouseX, mouseY);
        for (int i = 0; i < rows.size(); i++) {
            if (i != hit) rows.get(i).blur();
        }
        if (hit >= 0 && rows.get(hit).mouseClicked(mouseX, mouseY, button, rowX(), rowTop(hit), rowWidth())) {
            this.setFocused(null);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean showScrollbar() {
        return contentHeight() > listBottom - listTop;
    }

    private void dragScrollbar(double mouseY) {
        int visible = listBottom - listTop;
        int total = contentHeight();
        double t = Mth.clamp((mouseY - listTop) / visible, 0.0, 1.0);
        scroll = t * (total - visible);
        clampScroll();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (draggingScrollbar) {
            dragScrollbar(mouseY);
            return true;
        }
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).mouseDragged(mouseX, mouseY, rowX(), rowTop(i), rowWidth())) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        for (Row row : rows) row.mouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int hit = rowAt(mouseX, mouseY);
        if (hit >= 0 && rows.get(hit).mouseScrolled(mouseX, mouseY, scrollY, rowX(), rowTop(hit), rowWidth())) {
            return true;
        }
        if (mouseX >= listLeft && mouseX < listRight && mouseY >= listTop && mouseY < listBottom) {
            scroll -= scrollY * 18;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Row row : rows) {
            if (row.hasFocus()) {
                if (row.keyPressed(keyCode, scanCode, modifiers)) return true;
                // Escape leaves the text field instead of the whole screen
                if (keyCode == 256) {
                    row.blur();
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (Row row : rows) {
            if (row.hasFocus() && row.charTyped(chr, modifiers)) return true;
        }
        return super.charTyped(chr, modifiers);
    }

    // --------------------------------------------------------------- actions

    private void applyChanges() {
        for (var entry : workingConfig.getPlayerConfigs().entrySet()) {
            UUID uuid = getUuidFromCache(entry.getKey());
            if (uuid != null) {
                Config.PlayerAppearance pc = entry.getValue();
                pc.playerUuid = uuid;
                pc.playerName = entry.getKey();
            }
        }
        Config.copy(workingConfig, Config.getInstance());
        Config.getInstance().save();
        hasChanges = false;
        updateButtons();
    }

    private void updateButtons() {
        if (applyButton != null) applyButton.active = hasChanges;
        if (doneButton != null) doneButton.active = true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
