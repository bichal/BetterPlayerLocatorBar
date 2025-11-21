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

public final class TabBar extends AnimatedWidget {
    private final List<Tab> tabs = new ArrayList<>();
    private final Map<Integer, Transition> hoverTransitions = new HashMap<>();
    private final Transition selectionTransition = Constants.createTransition();
    private final IntConsumer onTabChange;
    private int selectedTab = 0;

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
        context.fill(getX(), getY(), getX() + width, getY() + height, 0xE0000000);
        context.drawBorder(getX(), getY(), width, height, 0xFF404040);

        int tabWidth = width / tabs.size();
        MinecraftClient mc = MinecraftClient.getInstance();

        selectionTransition.setTarget(selectedTab * tabWidth);
        float selectionX = selectionTransition.update();

        context.fill(getX() + (int) selectionX, getY() + height - 2, getX() + (int) selectionX + tabWidth, getY() + height, 0xFF00AA00);

        for (int i = 0; i < tabs.size(); i++) {
            int tabX = getX() + i * tabWidth;
            boolean hovered = mouseX >= tabX && mouseX < tabX + tabWidth && mouseY >= getY() && mouseY < getY() + height;

            Transition hover = hoverTransitions.get(i);
            hover.setTarget(hovered || i == selectedTab ? 1f : 0f);
            float hoverAlpha = hover.update();

            int bgAlpha = (int) (hoverAlpha * 40);
            context.fill(tabX, getY(), tabX + tabWidth, getY() + height, 0xFFFFFF | (bgAlpha << 24));

            Text title = tabs.get(i).title();
            int textX = tabX + (tabWidth - mc.textRenderer.getWidth(title)) / 2;
            int textColor = i == selectedTab ? 0xFFFFFF : 0xAAAAAA;
            context.drawText(mc.textRenderer, title, textX, getY() + 8, textColor, true);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int tabWidth = width / tabs.size();
        int clicked = (int) (mouseX - getX()) / tabWidth;
        if (clicked >= 0 && clicked < tabs.size()) {
            selectedTab = clicked;
            onTabChange.accept(clicked);
        }
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {}

    private record Tab(String key, Text title) {}
}