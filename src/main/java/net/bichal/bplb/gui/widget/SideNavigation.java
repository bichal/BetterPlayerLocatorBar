package net.bichal.bplb.gui.widget;

import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

public final class SideNavigation extends AnimatedWidget {
    private static final int COLLAPSED_WIDTH = 6;
    private static final int EXPANDED_WIDTH = 150;
    private final List<NavSection> sections = new ArrayList<>();
    private final Transition expandTransition = Constants.createTransition();
    private final Map<Integer, Transition> sectionHovers = new HashMap<>();
    private final IntConsumer onNavigate;
    private boolean expanded = false;

    public SideNavigation(int x, int y, int height, IntConsumer onNavigate) {
        super(x, y, COLLAPSED_WIDTH, height, Text.empty());
        this.setX(x);
        this.setY(y);
        this.onNavigate = onNavigate;
    }

    public void setSections(List<NavSection> sections) {
        this.sections.clear();
        this.sections.addAll(sections);
        sectionHovers.clear();
        for (int i = 0; i < sections.size(); i++) {
            sectionHovers.put(i, Constants.createHoverTransition());
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();

        expanded = mouseX >= x && mouseX < x + EXPANDED_WIDTH && mouseY >= y && mouseY < y + height;

        expandTransition.setTarget(expanded ? 1f : 0f);
        float expandProgress = expandTransition.update();

        int currentWidth = (int) (COLLAPSED_WIDTH + (EXPANDED_WIDTH - COLLAPSED_WIDTH) * expandProgress);
        setWidth(currentWidth);

        context.fill(x, y, x + currentWidth, y + height, 0xE8000000);
        context.drawBorder(x, y, currentWidth, height, 0xFF505050);

        if (expandProgress > 0.01f) renderSections(context, mouseY, expandProgress);
    }

    private void renderSections(DrawContext context, int mouseY, float alpha) {
        int x = getX();
        int y = getY();
        int height = getHeight();
        context.enableScissor(x, y, x + width, y + height);

        int yPos = y + 8;
        int sectionHeight = 26;
        int spacing = 3;
        MinecraftClient mc = MinecraftClient.getInstance();

        for (int i = 0; i < sections.size(); i++) {
            NavSection section = sections.get(i);
            boolean hovered = expanded && mouseY >= yPos && mouseY < yPos + sectionHeight;

            Transition hoverTrans = sectionHovers.get(i);
            hoverTrans.setTarget(hovered ? 1f : 0f);
            float hoverProgress = hoverTrans.update();

            int bgAlpha = (int) (hoverProgress * 80);
            context.fill(x + 3, yPos, x + width - 3, yPos + sectionHeight, 0x40FFFFFF | (bgAlpha << 24));

            int textAlpha = (int) (alpha * 255);
            int textColor = 0xFFFFFF | (textAlpha << 24);
            context.drawText(mc.textRenderer, section.title(), x + 8, yPos + 9, textColor, true);

            yPos += sectionHeight + spacing;
        }

        context.disableScissor();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int y = getY();
        if (!expanded) return;

        int yPos = y + 8;
        int sectionHeight = 26;
        int spacing = 3;

        for (NavSection section : sections) {
            if (mouseY >= yPos && mouseY < yPos + sectionHeight) {
                onNavigate.accept(section.targetY());
                break;
            }
            yPos += sectionHeight + spacing;
        }
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {}

    public record NavSection(Text title, int targetY) {}
}
