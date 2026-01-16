package net.bichal.bplb.gui.widget;

import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.util.ColorConstants;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

public final class TabBar extends AnimatedWidget {
    private final List<Tab> tabs = new ArrayList<>();
    private final Map<Integer, Transition> hoverTransitions = new HashMap<>();
    private final Transition selectionTransition = Constants.createTransition();
    private final IntConsumer onTabChange;
    public int selectedTab = 0;

    public TabBar(int x, int y, int width, IntConsumer onTabChange) {
        super(x, y, width, 24, Text.empty());
        this.onTabChange = onTabChange;
    }

    public void addTab(String key, Text title) {
        int index = tabs.size();
        tabs.add(new Tab(key, title));
        hoverTransitions.put(index, Constants.createHoverTransition());
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getX() + width, getY() + height, ColorConstants.COLOR_BLACK_OVERLAY);
        context.drawHorizontalLine(getX(), getX() + width, getY(), ColorConstants.COLOR_DARK_DISABLED);
        context.drawVerticalLine(getX(), getY(), getY() + height, ColorConstants.COLOR_DARK_DISABLED);
        context.drawVerticalLine(getX() + width - 1, getY(), getY() + height, ColorConstants.COLOR_DARK_DISABLED);

        int tabWidth = width / tabs.size();

        MinecraftClient mc = MinecraftClient.getInstance();
        selectionTransition.setTarget(selectedTab * tabWidth);
        float selectionX = selectionTransition.update();
        context.fill(getX() + (int) selectionX, getY() + height - 2, getX() + (int) selectionX + tabWidth, getY() + height, ColorConstants.COLOR_ENABLED);

        for (int i = 0; i < tabs.size(); i++) {
            int tabX = getX() + i * tabWidth;
            int nextTabX = tabX + tabWidth;

            boolean hovered = mouseX >= tabX && mouseX < nextTabX && mouseY >= getY() && mouseY < getY() + height;

            Transition hover = hoverTransitions.get(i);
            hover.setTarget(hovered || i == selectedTab ? 1f : 0f);
            float hoverAlpha = hover.update();

            int bgAlpha = (int) (hoverAlpha * 40);
            context.fill(tabX, getY(), nextTabX, getY() + height, 0xFFFFFF | (bgAlpha << 24));

            Text title = tabs.get(i).title();
            int titleWidth = mc.textRenderer.getWidth(title);
            int textX = tabX + (tabWidth - titleWidth) / 2;

            if (titleWidth > tabWidth - 4) {
                context.enableScissor(tabX, getY(), nextTabX, getY() + height);
            }

            int textColor = i == selectedTab ? ColorConstants.COLOR_WHITE : ColorConstants.COLOR_DISABLED;
            context.drawText(mc.textRenderer, title, textX, getY() + 8, textColor, true);

            if (titleWidth > tabWidth - 4) {
                context.disableScissor();
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int tabWidth = width / tabs.size();
        int clicked = (int) ((mouseX - getX()) / tabWidth);
        if (clicked >= 0 && clicked < tabs.size()) {
            selectedTab = clicked;
            onTabChange.accept(clicked);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    private record Tab(String key, Text title) {}
}
