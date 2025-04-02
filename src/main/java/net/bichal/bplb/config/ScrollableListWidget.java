package net.bichal.bplb.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ScrollableListWidget extends ElementListWidget<ScrollableListWidget.Entry> {
    private boolean scrolling;

    public ScrollableListWidget(MinecraftClient client, int width, int top, int bottom, int itemHeight) {
        super(client, width, bottom - top, top, itemHeight);
        this.centerListVertically = false;
    }

    @Override
    public int getScrollbarX() {
        return this.width - 6;
    }

    @Override
    public int getRowWidth() {
        return this.width - (Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4)) > 0 ? 18 : 12);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updateScrollingState(mouseX, mouseY, button);
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        } else {
            Entry entry = this.getEntryAtPos(mouseX, mouseY);
            if (entry != null) {
                if (entry.mouseClicked(mouseX, mouseY, button)) {
                    this.setFocused(entry);
                    this.setDragging(true);
                    return true;
                }
            } else if (button == 0 && this.clickedHeader((int) (mouseX - (double) (this.getX() + this.width / 2 - this.getRowWidth() / 2)), (int) (mouseY - (double) this.getY()) + (int) this.getScrollAmount() - 4)) {
                return true;
            }

            return this.scrolling;
        }
    }

    public final Entry getEntryAtPos(double x, double y) {
        int i = MathHelper.floor(y - (double) this.getY()) - this.headerHeight + (int) this.getScrollAmount() - 4;
        int index = i / this.itemHeight;
        return x < (double) this.getScrollbarX() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && i >= 0 && index < this.getEntryCount() ? this.children().get(index) : null;
    }

    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 && mouseX >= this.getScrollbarX() && mouseX < this.getScrollbarX() + 6;
    }

    public void addPublicEntry(Entry entry) {
        super.addEntry(entry);
    }

    public abstract static class Entry extends ElementListWidget.Entry<ScrollableListWidget.Entry> {
        @Override
        public List<? extends Element> children() {
            return new ArrayList<>();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return new ArrayList<>();
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return false;
        }

        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return false;
        }
    }
}
