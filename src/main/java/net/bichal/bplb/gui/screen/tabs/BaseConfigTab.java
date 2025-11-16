package net.bichal.bplb.gui.screen.tabs;

import net.bichal.bplb.gui.Config;
import net.bichal.bplb.gui.ConfigScreen;
import net.bichal.bplb.gui.widget.ScrollableListWidget;
import net.bichal.bplb.gui.widget.SideNavigationWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public abstract class BaseConfigTab extends Screen {
    protected final ConfigScreen parent;
    protected final Config workingConfig;
    protected ScrollableListWidget scrollableList;
    protected SideNavigationWidget sideNav;

    protected BaseConfigTab(ConfigScreen parent, Config workingConfig) {
        super(Text.empty());
        this.parent = parent;
        this.workingConfig = workingConfig;
    }

    protected void initScrollableList() {
        int uiWidth = Math.min(this.width - 100, 460);
        this.scrollableList = new ScrollableListWidget(this.client, this.width, 85, this.height - 35, 24);
        this.scrollableList.setRowWidth(uiWidth);
        this.scrollableList.setRowLeft(this.width / 2 - uiWidth / 2);
        this.scrollableList.setScrollbarX(this.width - 6);
    }

    protected void initSideNav(List<SideNavigationWidget.SectionDefinition> sections) {
        if (sections.isEmpty()) return;
        this.sideNav = new SideNavigationWidget(5, 85, this.height - 120, sections, targetY -> {
            if (scrollableList != null) {
                scrollableList.setScrollAmount(targetY - 85);
            }
        });
        this.addDrawableChild(sideNav);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}
    protected abstract void populateOptions();
}