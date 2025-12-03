package net.bichal.bplb.gui.widget.entry;

import net.bichal.bplb.gui.widget.CompactButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Consumer;

public final class SliderEntry extends BaseConfigEntry {
    private static final int SLIDER_WIDTH = 100;
    private static final int SLIDER_HEIGHT = 14;
    private static final int PADDING = 10;
    private final float min, max, defaultValue;
    private final Consumer<Float> onChange;
    private final boolean isInteger;
    private final CompactButton resetButton;
    private float value;
    private boolean dragging = false;

    public SliderEntry(MinecraftClient client, String key, Text label, float initial, float min, float max, Consumer<Float> onChange, boolean isInteger) {
        super(client, key, label);
        this.value = initial;
        this.defaultValue = initial;
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        this.isInteger = isInteger;
        this.resetButton = CompactButton.texture(0, 0, 20, 40, 0, false, b -> {
            value = defaultValue;
            onChange.accept(value);
        });
    }

    @Override
    protected void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        context.drawText(client.textRenderer, getDisplayLabel(), x + PADDING, y + (height - 8) / 2, 0xFFFFFF, true);

        String valueText = formatValue(value);
        int valueWidth = client.textRenderer.getWidth(valueText);
        int sliderX = x + width - SLIDER_WIDTH - PADDING;
        int valueX = sliderX - valueWidth - 8;

        resetButton.setX(valueX - 28);
        resetButton.setY(y + (height - 20) / 2);
        resetButton.active = Math.abs(value - defaultValue) > 0.01f;
        if (resetButton.active) resetButton.render(context, mouseX, mouseY, delta);

        int sliderY = y + (height - SLIDER_HEIGHT) / 2;

        context.fill(sliderX, sliderY + SLIDER_HEIGHT / 2 - 1, sliderX + SLIDER_WIDTH, sliderY + SLIDER_HEIGHT / 2 + 1, 0xFF404040);

        float normalizedValue = (value - min) / (max - min);
        int fillWidth = (int) (SLIDER_WIDTH * normalizedValue);
        context.fill(sliderX, sliderY + SLIDER_HEIGHT / 2 - 1, sliderX + fillWidth, sliderY + SLIDER_HEIGHT / 2 + 1, 0xFF00AA00);

        int thumbX = sliderX + (int) ((SLIDER_WIDTH - 6) * normalizedValue);
        context.fill(thumbX, sliderY, thumbX + 6, sliderY + SLIDER_HEIGHT, 0xFFFFFFFF);
        context.drawBorder(thumbX, sliderY, 6, SLIDER_HEIGHT, 0xFF000000);

        context.drawText(client.textRenderer, valueText, valueX, y + (height - 8) / 2, 0xAAFFAA, false);
    }

    private String formatValue(float val) {
        if (isInteger) {
            return String.valueOf((int) val);
        }
        String formatted = String.format("%.2f", val);
        if (formatted.endsWith("0")) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }
        if (formatted.endsWith(".0")) {
            formatted = formatted.substring(0, formatted.length() - 2);
        }
        return formatted;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (resetButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0) {
            int sliderX = getSliderX();
            if (mouseX >= sliderX && mouseX <= sliderX + SLIDER_WIDTH) {
                dragging = true;
                updateValue(mouseX, sliderX);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            updateValue(mouseX, getSliderX());
            return true;
        }
        return false;
    }

    private int getSliderX() {
        if (client == null) return 0;
        int rowWidth = Math.min(client.getWindow().getScaledWidth() - 20, 460);
        int parentX = (client.getWindow().getScaledWidth() - rowWidth) / 2;
        return parentX + rowWidth - SLIDER_WIDTH - PADDING;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 262 || keyCode == 263) {
            float step = (max - min) * 0.01f;
            if (isInteger) step = Math.max(1, step);
            if (keyCode == 262) {
                value = Math.min(max, value + step);
            } else {
                value = Math.max(min, value - step);
            }
            if (isInteger) value = Math.round(value);
            onChange.accept(value);
            return true;
        }
        return false;
    }

    private void updateValue(double mouseX, int sliderX) {
        float normalized = MathHelper.clamp((float) (mouseX - sliderX) / SLIDER_WIDTH, 0f, 1f);
        value = min + (max - min) * normalized;
        if (isInteger) value = Math.round(value);
        onChange.accept(value);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of(resetButton);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(resetButton);
    }
}
