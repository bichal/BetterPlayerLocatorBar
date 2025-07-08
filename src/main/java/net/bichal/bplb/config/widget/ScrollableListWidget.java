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
    }

    int rowWidth;
    int rowLeft;
    int scrollbarX;

    public void tick() {
        this.children().forEach(Entry::tick);
    }

    public void clearEntries() {
        super.clearEntries();
    }

    @Override public int getScrollbarX() {
        return scrollbarX;
    }

    public void setScrollbarX(int scrollbarX) {
        this.scrollbarX = scrollbarX;
    }

    @Override public int getRowWidth() {
        return rowWidth;
    }

    public void setRowWidth(int rowWidth) {
        this.rowWidth = rowWidth;
    }

    @Override public int getRowLeft() {
        return rowLeft;
    }

    public void setRowLeft(int rowLeft) {
        this.rowLeft = rowLeft;
    }

    public abstract static class Entry extends ElementListWidget.Entry<Entry> {
        protected MinecraftClient client;

        public void tick() {
        }

        @Override public List<? extends Element> children() {
            return new ArrayList<>();
        }

        @Override public List<? extends Selectable> selectableChildren() {
            return new ArrayList<>();
        }
    }

    public void addPublicEntry(Entry entry) {
        super.addEntry(entry);
    }
}
