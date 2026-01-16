package net.bichal.bplb.gui.widget.entry;

import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.gui.widget.CompactButton;
import net.bichal.bplb.util.ColorConstants;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public final class ToggleEntry extends BaseConfigEntry {
    private static final int TOGGLE_WIDTH = 36;
    private static final int TOGGLE_HEIGHT = 18;
    private final Transition toggleTransition;
    private final Consumer<Boolean> onChange;
    private final boolean defaultValue;
    private final CompactButton resetButton;
    private boolean value;
    private int toggleX, toggleY;

    public ToggleEntry(MinecraftClient client, String key, Text label, boolean initial, Consumer<Boolean> onChange) {
        super(client, key, label);
        this.value = initial;
        this.defaultValue = initial;
        this.onChange = onChange;
        this.toggleTransition = Constants.createToggleTransition();
        this.resetButton = CompactButton.texture(0, 0, 20, 40, 0, false, b -> {
            value = defaultValue;
            onChange.accept(value);
        });
    }

    @Override
    protected void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        context.drawText(client.textRenderer, getDisplayLabel(), x + 10, y + (height - 8) / 2, 0xFFFFFF, true);

        resetButton.setX(x + width - 70);
        resetButton.setY(y + (height - 20) / 2);
        resetButton.active = (value != defaultValue);
        if (resetButton.active) resetButton.render(context, mouseX, mouseY, delta);

        toggleX = x + width - TOGGLE_WIDTH - 10;
        toggleY = y + (height - TOGGLE_HEIGHT) / 2;

        toggleTransition.setTarget(value ? 1f : 0f);
        float progress = toggleTransition.update();

        int trackColor = lerpColor(0xFF555555, 0xFF00AA00, progress);
        context.fill(toggleX, toggleY, toggleX + TOGGLE_WIDTH, toggleY + TOGGLE_HEIGHT, 0xFF000000);
        context.fill(toggleX + 1, toggleY + 1, toggleX + TOGGLE_WIDTH - 1, toggleY + TOGGLE_HEIGHT - 1, trackColor);

        int thumbSize = TOGGLE_HEIGHT - 4;
        int thumbX = toggleX + 2 + (int) ((TOGGLE_WIDTH - thumbSize - 4) * progress);
        context.fill(thumbX, toggleY + 2, thumbX + thumbSize, toggleY + TOGGLE_HEIGHT - 2, ColorConstants.COLOR_WHITE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (resetButton.mouseClicked(mouseX, mouseY, button)) return true;

        if (button == 0 && mouseX >= toggleX && mouseX <= toggleX + TOGGLE_WIDTH &&
                mouseY >= toggleY && mouseY <= toggleY + TOGGLE_HEIGHT) {
            value = !value;
            toggleTransition.setTarget(value ? 1f : 0f);
            onChange.accept(value);
            return true;
        }
        return false;
    }

    private int lerpColor(int c1, int c2, float t) {
        int it = (int) (t * 256);
        int invT = 256 - it;

        int rb1 = c1 & 0x00FF00FF;
        int ag1 = (c1 >>> 8) & 0x00FF00FF;
        int rb2 = c2 & 0x00FF00FF;
        int ag2 = (c2 >>> 8) & 0x00FF00FF;

        int rb = ((rb1 * invT + rb2 * it) >>> 8) & 0x00FF00FF;
        int ag = ((ag1 * invT + ag2 * it)) & 0xFF00FF00;

        return ag | rb;
    }

    @Override
    public List<? extends Selectable> selectableChildren() {return List.of();}

    @Override
    public List<? extends Element> children() {return List.of();}
}