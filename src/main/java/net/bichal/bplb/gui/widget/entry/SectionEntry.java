package net.bichal.bplb.gui.widget.entry;

import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.util.ColorConstants;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;

import java.util.List;
import java.util.function.Consumer;

public final class SectionEntry extends BaseConfigEntry {
    private final Transition expandTransition = Constants.createTransition();
    private final Consumer<Boolean> onToggle;
    private boolean expanded;
    private boolean collapsible;

    public SectionEntry(MinecraftClient client, Text title, boolean initialExpanded, Consumer<Boolean> onToggle) {
        super(client, "", title);
        this.expanded = initialExpanded;
        this.onToggle = onToggle;
        this.collapsible = true;
        expandTransition.setTarget(expanded ? 1f : 0f);
    }

    public void setCollapsible(boolean collapsible) {
        this.collapsible = collapsible;
        if (!collapsible) {
            expanded = true;
            expandTransition.setTarget(1f);
        }
    }

    @Override
    protected void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        context.fill(x + 4, y, x + width - 4, y + height, ColorConstants.COLOR_BLACK_OVERLAY);
        context.drawHorizontalLine(x + 4, x + width - 5, y + height - 1, ColorConstants.COLOR_DISABLED_OVERLAY);

        if (collapsible) {
            expandTransition.setTarget(expanded ? 1f : 0f);
            float progress = expandTransition.update();

            context.getMatrices().push();
            context.getMatrices().translate(x + 12, y + height / 2f, 0);
            context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(progress * 90));
            context.drawText(client.textRenderer, "▶", -4, -4, ColorConstants.COLOR_WHITE, false);
            context.getMatrices().pop();

            context.drawText(client.textRenderer, label, x + 30, y + (height - 8) / 2, ColorConstants.COLOR_WHITE, true);
        } else {
            context.drawText(client.textRenderer, label, x + 12, y + (height - 8) / 2, ColorConstants.COLOR_WHITE, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && collapsible) {
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
