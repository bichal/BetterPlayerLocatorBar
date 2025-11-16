package net.bichal.bplb.gui.widget;

import net.bichal.bplb.gui.screen.BaseTabScreen;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Consumer;

public class TabNavigationWidget extends ClickableWidget {
    private final List<BaseTabScreen.TabDefinition> tabs;
    private int selectedTab;
    private final Consumer<Integer> onTabChange;
    private final float[] tabHoverProgress;
    
    public TabNavigationWidget(int x, int y, int width, int height, List<BaseTabScreen.TabDefinition> tabs, int initialTab, Consumer<Integer> onTabChange) {
        super(x, y, width, height, Text.empty());
        this.tabs = tabs;
        this.selectedTab = initialTab;
        this.onTabChange = onTabChange;
        this.tabHoverProgress = new float[tabs.size()];
    }

    public void updatePosition(int x, int y, int width) {
        this.setX(x);
        this.setY(y);
        this.width = width;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int tabWidth = this.width / tabs.size();
        MinecraftClient client = MinecraftClient.getInstance();
        
        for (int i = 0; i < tabs.size(); i++) {
            int tabX = this.getX() + i * tabWidth;
            boolean hovered = mouseX >= tabX && mouseX < tabX + tabWidth && mouseY >= this.getY() && mouseY < this.getY() + this.height;
            boolean selected = i == selectedTab;
            
            float targetHover = (hovered || selected) ? 1f : 0f;
            tabHoverProgress[i] = MathHelper.lerp(0.2f, tabHoverProgress[i], targetHover);
            
            int bgColor = selected ? 0xFF2A2A2A : 0xFF1A1A1A;
            int brightness = (int) (tabHoverProgress[i] * 20);
            bgColor = (bgColor & 0xFF000000) | ((bgColor & 0xFF0000) + (brightness << 16)) | ((bgColor & 0xFF00) + (brightness << 8)) | ((bgColor & 0xFF) + brightness);
            
            context.fill(tabX, this.getY(), tabX + tabWidth - 1, this.getY() + this.height, bgColor);
            
            if (selected) {
                context.fill(tabX, this.getY() + this.height - 2, tabX + tabWidth - 1, this.getY() + this.height, 0xFF55FF55);
            }
            
            Text tabText = Text.translatable(Constants.CONFIG_KEY_PREFIX + "tab." + tabs.get(i).key());
            int textX = tabX + (tabWidth - client.textRenderer.getWidth(tabText)) / 2;
            int textColor = selected ? 0xFFFFFF : 0xAAAAAA;
            context.drawTextWithShadow(client.textRenderer, tabText, textX, this.getY() + 6, textColor);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int tabWidth = this.width / tabs.size();
        int clickedTab = (int) ((mouseX - this.getX()) / tabWidth);
        if (clickedTab >= 0 && clickedTab < tabs.size()) {
            selectedTab = clickedTab;
            onTabChange.accept(selectedTab);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}