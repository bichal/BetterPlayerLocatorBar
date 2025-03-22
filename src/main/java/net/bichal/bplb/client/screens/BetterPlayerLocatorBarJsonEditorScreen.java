package net.bichal.bplb.client.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import net.bichal.bplb.BetterPlayerLocatorBar;
import net.bichal.bplb.client.widgets.TextAreaWidget;
import net.bichal.bplb.config.BetterPlayerLocatorBarConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class BetterPlayerLocatorBarJsonEditorScreen extends Screen {
    private static final int LINE_HEIGHT = 12;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Screen parent;
    private TextAreaWidget jsonEditor;
    private String originalJson;
    private final List<String> undoHistory = new ArrayList<>();
    private final List<String> redoHistory = new ArrayList<>();
    private boolean showPreview = true;
    private long lastApplyTime = 0;
    private String errorMessage = null;
    private TooltipManager tooltipManager;

    public BetterPlayerLocatorBarJsonEditorScreen(Screen parent) {
        super(Text.translatable("screen.bplb.json_editor.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        tooltipManager = new TooltipManager();

        int editorWidth = width - 40;
        int editorHeight = height - 120;

        BetterPlayerLocatorBarConfig config = BetterPlayerLocatorBarConfig.getInstance();
        originalJson = GSON.toJson(config);

        jsonEditor = new TextAreaWidget(
                this.textRenderer,
                20, 30, editorWidth, editorHeight,
                Text.literal("JSON Editor")
        );
        jsonEditor.setText(formatJsonWithComments(originalJson));
        addDrawableChild(jsonEditor);

        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonSpacing = 10;
        int y = height - 60;

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bplb.json_editor.back"), button -> Objects.requireNonNull(client).setScreen(parent)).dimensions(20, y, buttonWidth, buttonHeight).build());

        ButtonWidget applyButton = ButtonWidget.builder(Text.translatable("screen.bplb.json_editor.apply"), button -> applyChanges()).dimensions(20 + buttonWidth + buttonSpacing, y, buttonWidth, buttonHeight).build();
        addDrawableChild(applyButton);

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bplb.json_editor.reset"), button -> {
            saveToUndoHistory();
            jsonEditor.setText(formatJsonWithComments(originalJson));
            errorMessage = null;
        }).dimensions(20 + (buttonWidth + buttonSpacing) * 2, y, buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable(showPreview ? "screen.bplb.json_editor.hide_preview" : "screen.bplb.json_editor.show_preview"),
                button -> {
                    showPreview = !showPreview;
                    button.setMessage(Text.translatable(showPreview ? "screen.bplb.json_editor.hide_preview" : "screen.bplb.json_editor.show_preview"));
                }).dimensions(20 + (buttonWidth + buttonSpacing) * 3, y, buttonWidth, buttonHeight).build());

        ButtonWidget undoButton = ButtonWidget.builder(Text.translatable("screen.bplb.json_editor.undo"), button -> undo()).dimensions(20, y + buttonHeight + 5, buttonWidth, buttonHeight).build();
        undoButton.setTooltip(Tooltip.of(Text.literal("Ctrl+Z")));
        addDrawableChild(undoButton);

        ButtonWidget redoButton = ButtonWidget.builder(Text.translatable("screen.bplb.json_editor.redo"), button -> redo()).dimensions(20 + buttonWidth + buttonSpacing, y + buttonHeight + 5, buttonWidth, buttonHeight).build();
        redoButton.setTooltip(Tooltip.of(Text.literal("Ctrl+Y or Ctrl+Shift+Z")));
        addDrawableChild(redoButton);

        saveToUndoHistory();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        context.fill(10, 20, width - 10, height - 80, 0x80000000);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        if (errorMessage != null) {
            context.fill(20, height - 80, width - 20, height - 65, 0xFFFF0000);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(errorMessage), this.width / 2, height - 77, 0xFFFFFF);
        }

        long timeSinceApply = System.currentTimeMillis() - lastApplyTime;
        if (timeSinceApply < 1000) {
            float alpha = 1.0f - (timeSinceApply / 1000f);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.bplb.json_editor.applied"), this.width / 2, 30, 0xFF00FF00);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        tooltipManager.renderTooltip(context, mouseX, mouseY, jsonEditor);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_Z && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && (modifiers & GLFW.GLFW_MOD_SHIFT) == 0) {
            undo();
            return true;
        }

        if ((keyCode == GLFW.GLFW_KEY_Y && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) ||
                (keyCode == GLFW.GLFW_KEY_Z && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0)) {
            redo();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_S && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            applyChanges();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void saveToUndoHistory() {
        if (jsonEditor != null) {
            undoHistory.add(jsonEditor.getText());
            if (undoHistory.size() > 50) {
                undoHistory.removeFirst();
            }
            redoHistory.clear();
        }
    }

    private void undo() {
        if (undoHistory.size() > 1) {
            String current = jsonEditor.getText();
            redoHistory.add(current);
            undoHistory.removeLast();
            String previous = undoHistory.getLast();
            jsonEditor.setText(previous);
        }
    }

    private void redo() {
        if (!redoHistory.isEmpty()) {
            String current = jsonEditor.getText();
            undoHistory.add(current);
            String next = redoHistory.removeLast();
            jsonEditor.setText(next);
        }
    }

    private void applyChanges() {
        try {
            String jsonText = jsonEditor.getText().replaceAll("//.*?\\n", "\n");
            JsonElement element = JsonParser.parseString(jsonText);

            BetterPlayerLocatorBarConfig config = GSON.fromJson(element, BetterPlayerLocatorBarConfig.class);

            BetterPlayerLocatorBarConfig.getInstance().copyFrom(config);
            BetterPlayerLocatorBarConfig.getInstance().saveConfig();

            lastApplyTime = System.currentTimeMillis();
            errorMessage = null;

            saveToUndoHistory();
        } catch (Exception e) {
            errorMessage = "Error: " + e.getMessage();
            BetterPlayerLocatorBar.LOGGER.error("[{}] JSON parsing error: {}", BetterPlayerLocatorBar.MOD_SHORT_NAME, e.getMessage());
        }
    }

    private String formatJsonWithComments(String json) {
        StringBuilder enhanced = new StringBuilder();
        String[] lines = json.split("\n");

        for (String line : lines) {
            enhanced.append(line).append("\n");

            if (line.contains("\"minAlpha\":")) {
                enhanced.append("// Controls the minimum opacity for players at maximum fade distance\n");
            } else if (line.contains("\"maxFadeDistance\":")) {
                enhanced.append("// Maximum distance in blocks before players fade to minimum opacity\n");
            } else if (line.contains("\"fadeStartDistance\":")) {
                enhanced.append("// Distance in blocks where players start to fade from full opacity\n");
            } else if (line.contains("\"lerpSpeed\":")) {
                enhanced.append("// Speed at which opacity changes when player distance changes\n");
            } else if (line.contains("\"iconSize\":")) {
                enhanced.append("// Size of player icons on the locator bar\n");
            } else if (line.contains("\"iconOpacity\":")) {
                enhanced.append("// Base opacity value for player icons\n");
            } else if (line.contains("\"playerSettings\":")) {
                enhanced.append("// Custom settings for individual players\n");
            }
        }

        return enhanced.toString();
    }

    private class TooltipManager {
        private static final int TOOLTIP_WIDTH = 200;
        private static final int TOOLTIP_PADDING = 5;

        public void renderTooltip(DrawContext context, int mouseX, int mouseY, TextAreaWidget editor) {
            if (!editor.isMouseOver(mouseX, mouseY)) return;

            int relativeY = mouseY - editor.getY();
            int scrollOffset = 0;
            int lineIndex = relativeY / LINE_HEIGHT + scrollOffset;
            String line = getLineAtIndex(editor.getText(), lineIndex);

            if (line == null) return;

            String tooltipText = getString(line);

            if (tooltipText != null) {
                int tooltipX = mouseX + 12;

                if (tooltipX + TOOLTIP_WIDTH > width) {
                    tooltipX = width - TOOLTIP_WIDTH - 5;
                }

                List<Text> tooltipLines = wrapTooltipText(tooltipText);
                int tooltipHeight = tooltipLines.size() * 10 + TOOLTIP_PADDING * 2;

                context.fill(tooltipX, mouseY, tooltipX + TOOLTIP_WIDTH, mouseY + tooltipHeight, 0xF0100010);
                context.fill(tooltipX + 1, mouseY + 1, tooltipX + TOOLTIP_WIDTH - 1, mouseY + tooltipHeight - 1, 0xF0383838);

                for (int i = 0; i < tooltipLines.size(); i++) {
                    context.drawText(textRenderer, tooltipLines.get(i), tooltipX + TOOLTIP_PADDING, mouseY + TOOLTIP_PADDING + i * 10, 0xFFFFFF, false);
                }
            }
        }

        private static @Nullable String getString(String line) {
            String tooltipText = null;
            if (line.contains("\"minAlpha\":")) {
                tooltipText = "Controls the minimum opacity for players when they are at the maximum fade distance.";
            } else if (line.contains("\"maxFadeDistance\":")) {
                tooltipText = "Maximum distance in blocks before players fade completely to the minimum opacity value.";
            } else if (line.contains("\"fadeStartDistance\":")) {
                tooltipText = "Distance in blocks at which players start to fade from full opacity.";
            } else if (line.contains("\"playerSettings\":")) {
                tooltipText = "Contains custom settings for individual players, including colors and visibility options.";
            } else if (line.contains("\"color\":")) {
                tooltipText = "Player color in ARGB format (Alpha, Red, Green, Blue). Example: -1 for white.";
            } else if (line.contains("\"enabled\":")) {
                tooltipText = "Whether this player is shown on the locator bar.";
            }
            return tooltipText;
        }

        private String getLineAtIndex(String text, int index) {
            String[] lines = text.split("\n");
            if (index >= 0 && index < lines.length) {
                return lines[index];
            }
            return null;
        }

        private List<Text> wrapTooltipText(String text) {
            List<Text> lines = new ArrayList<>();
            String[] words = text.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (textRenderer.getWidth(currentLine + " " + word) > TooltipManager.TOOLTIP_WIDTH - TOOLTIP_PADDING * 2) {
                    if (!currentLine.isEmpty()) {
                        lines.add(Text.literal(currentLine.toString()));
                        currentLine = new StringBuilder(word);
                    } else {
                        lines.add(Text.literal(word));
                    }
                } else {
                    if (!currentLine.isEmpty()) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(Text.literal(currentLine.toString()));
            }

            return lines;
        }
    }
}