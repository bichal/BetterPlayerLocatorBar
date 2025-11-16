package net.bichal.bplb.gui.widget.entries;

import net.bichal.bplb.gui.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class InfoTextEntry extends ScrollableListWidget.Entry {
    private final MinecraftClient client;
    private final Text[] lines;
    
    public InfoTextEntry(MinecraftClient client, Text... lines) {
        this.client = client;
        this.lines = lines;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int currentY = y + 4;
        for (Text line : lines) {
            context.drawTextWithShadow(this.client.textRenderer, line, x + Constants.CONFIG_PADDING, currentY, Constants.WHITE_COLOR);
            currentY += this.client.textRenderer.fontHeight + 2;
        }
    }
}