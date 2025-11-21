package net.bichal.bplb.gui.widget.entry;

import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public final class SectionEntry extends BaseConfigEntry {
    private final Transition expandTransition = Constants.createTransition();
    private final Transition flashTransition = Constants.createFlashTransition();
    private final Consumer<Boolean> onToggle;
    private boolean expanded;
    private long flashStartTime = -1;

    public SectionEntry(MinecraftClient client, Text title, boolean initialExpanded, Consumer<Boolean> onToggle) {
        super(client, "", title);
        this.expanded = initialExpanded;
        this.onToggle = onToggle;
        expandTransition.setTarget(expanded ? 1f : 0f);
    }

    public void triggerFlash() {
        flashStartTime = System.currentTimeMillis();
        flashTransition.setTarget(1f);
    }

    @Override
    protected void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        if (flashStartTime > 0) {
            long elapsed = System.currentTimeMillis() - flashStartTime;
            if (elapsed > Constants.SECTION_FLASH_DURATION_MS) {
                flashStartTime = -1;
                flashTransition.setTarget(0f);
            } else {
                float progress = 1f - (elapsed / (float) Constants.SECTION_FLASH_DURATION_MS);
                float pulse = (float) Math.sin(elapsed * 0.008) * 0.5f + 0.5f;
                int flashAlpha = (int) (pulse * progress * 96);
                context.fill(x + 4, y, x + width - 4, y + height, Constants.COLOR_FLASH_OVERLAY | (flashAlpha << 24));
            }
        }

        expandTransition.setTarget(expanded ? 1f : 0f);
        float progress = expandTransition.update();

        context.fill(x + 4, y, x + width - 4, y + height, 0x40000000);
        context.drawHorizontalLine(x + 4, x + width - 4, y + height - 1, 0xFF404040);

        context.getMatrices().push();
        context.getMatrices().translate(x + 12, y + height / 2f, 0);
        context.getMatrices().multiply(
                net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(progress * 90)
        );
        context.drawText(client.textRenderer, "▶", -4, -4, 0xFFFFFF, false);
        context.getMatrices().pop();

        context.drawText(client.textRenderer, label, x + 30, y + (height - 8) / 2, 0xFFFFFF, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            expanded = !expanded;
            onToggle.accept(expanded);
            return true;
        }
        return false;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        expandTransition.setTarget(expanded ? 1f : 0f);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of();
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }
}