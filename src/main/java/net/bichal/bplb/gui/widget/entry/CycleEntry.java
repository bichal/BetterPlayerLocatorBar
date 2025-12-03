package net.bichal.bplb.gui.widget.entry;

import net.bichal.bplb.gui.widget.CompactButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public final class CycleEntry extends BaseConfigEntry {
    private final List<String> options;
    private final Consumer<String> onChange;
    private final CompactButton cycleButton;
    private final CompactButton resetButton;
    private final String defaultValue;
    private int currentIndex;

    public CycleEntry(MinecraftClient client, String key, Text label, String initial, List<String> options, Consumer<String> onChange) {
        super(client, key, label);
        this.options = options;
        this.defaultValue = initial;
        this.onChange = onChange;
        this.currentIndex = options.indexOf(initial);
        if (this.currentIndex < 0) this.currentIndex = 0;

        this.cycleButton = CompactButton.text(0, 0, 100, 20, Text.literal(options.get(currentIndex)), b -> cycle());

        this.resetButton = CompactButton.texture(0, 0, 20, 40, 0, false, b -> {
            currentIndex = options.indexOf(defaultValue);
            if (currentIndex < 0) currentIndex = 0;
            cycleButton.setMessage(Text.literal(options.get(currentIndex)));
            onChange.accept(options.get(currentIndex));
        });
    }

    private void cycle() {
        currentIndex = (currentIndex + 1) % options.size();
        updateButton();
    }

    private void cycleBack() {
        currentIndex = (currentIndex - 1 + options.size()) % options.size();
        updateButton();
    }

    private void updateButton() {
        cycleButton.setMessage(Text.literal(options.get(currentIndex)));
        onChange.accept(options.get(currentIndex));
    }

    @Override
    protected void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        context.drawText(client.textRenderer, getDisplayLabel(), x + 10, y + (height - 8) / 2, 0xFFFFFF, true);

        resetButton.setX(x + width - 130);
        resetButton.setY(y + (height - 20) / 2);
        String currentValue = options.get(currentIndex);
        resetButton.active = !currentValue.equals(defaultValue);
        if (resetButton.active) resetButton.render(context, mouseX, mouseY, delta);

        cycleButton.setX(x + width - 110);
        cycleButton.setY(y + (height - 20) / 2);
        cycleButton.render(context, mouseX, mouseY, delta);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (resetButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (cycleButton.isMouseOver(mouseX, mouseY)) {
            if (button == 0) {
                cycle();
                return true;
            } else if (button == 1) {
                cycleBack();
                return true;
            }
        }
        return false;
    }

    @Override public List<? extends Selectable> selectableChildren() {
        return List.of(cycleButton, resetButton);
    }

    @Override public List<? extends Element> children() {
        return List.of(cycleButton, resetButton);
    }
}
