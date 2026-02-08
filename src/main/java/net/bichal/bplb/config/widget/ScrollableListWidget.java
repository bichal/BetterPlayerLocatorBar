package net.bichal.bplb.config.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.ArrayList;
import java.util.List;

public class ScrollableListWidget extends ElementListWidget<ScrollableListWidget.Entry> {
    public ScrollableListWidget(MinecraftClient client, int width, int top, int bottom, int itemHeight) {
        super(client, width, bottom - top, top, itemHeight);
        this.centerListVertically = false;
        this.headerHeight = 0;
    }

    int rowWidth;
    int scrollbarX;

    public void tick() {
        this.children().forEach(Entry::tick);
    }
    public void clearEntries() {
        super.clearEntries();
    }
    @Override
    public int getScrollbarX() {
        return scrollbarX;
    }
    public void setScrollbarX(int scrollbarX) {
        this.scrollbarX = scrollbarX;
    }
    @Override
    public int getRowWidth() {
        return rowWidth;
    }
    public void setRowWidth(int rowWidth) {
        this.rowWidth = rowWidth;
    }

    public double getScrollAmount() {
        return this.getScrollY();
    }

    public void setScrollAmount(double scroll) {
        this.setScrollY((int) scroll);
    }

    public void addPublicEntry(Entry entry) {
        super.addEntry(entry);
    }

    public abstract static class Entry extends ElementListWidget.Entry<Entry> {
        public void tick() {
        }

        @Override
        public List<? extends Element> children() {
            return new ArrayList<>();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return new ArrayList<>();
        }
    }

    @Override
    protected void renderHeader(net.minecraft.client.gui.DrawContext context, int x, int y) {
    }

    @Override
    protected void drawHeaderAndFooterSeparators(net.minecraft.client.gui.DrawContext context) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.getFocused() instanceof Entry entry) {
            if (entry.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.getFocused() instanceof Entry entry) {
            if (entry.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }
}
