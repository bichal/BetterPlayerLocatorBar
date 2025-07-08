package net.bichal.bplb.config.widget.entries;

import net.bichal.bplb.config.widget.ScrollableListWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class SectionHeaderEntry extends ScrollableListWidget.Entry {
    private final MinecraftClient client;
    private final Text title;

    public SectionHeaderEntry(MinecraftClient client, Text title) {
        this.client = client;
        this.title = title;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        context.drawCenteredTextWithShadow(this.client.textRenderer, title, x + entryWidth / 2, y + 6, 0xFFFFFF);
        int textWidth = this.client.textRenderer.getWidth(title) / 2 + 10;
        context.fill(x + Constants.CONFIG_PADDING, y + 9, x + entryWidth / 2 - textWidth - 10, y + 10, 0x30FFFFFF);
        context.fill(x + Constants.CONFIG_PADDING, y + 10, x + entryWidth / 2 - textWidth - 10, y + 11, 0x40000000);
        context.fill(x + entryWidth / 2 - textWidth - 8, y + 7, x + entryWidth / 2 - textWidth - 7, y + 12, 0x30FFFFFF);
        context.fill(x + entryWidth / 2 - textWidth - 8, y + 12, x + entryWidth / 2 - textWidth - 7, y + 13, 0x40000000);
        context.fill(x + entryWidth / 2 + textWidth + 10, y + 9, x + entryWidth - Constants.CONFIG_PADDING, y + 10, 0x30FFFFFF);
        context.fill(x + entryWidth / 2 + textWidth + 10, y + 10, x + entryWidth - Constants.CONFIG_PADDING, y + 11, 0x40000000);
        context.fill(x + entryWidth / 2 + textWidth + 8, y + 7, x + entryWidth / 2 + textWidth + 7, y + 12, 0x30FFFFFF);
        context.fill(x + entryWidth / 2 + textWidth + 8, y + 12, x + entryWidth / 2 + textWidth + 7, y + 13, 0x40000000);
    }
}
