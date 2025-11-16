package net.bichal.bplb.gui.screen;

import net.bichal.bplb.gui.widget.TabNavigationWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseTabScreen extends Screen {
    protected final Screen parent;
    protected TabNavigationWidget tabNavigation;
    protected final List<TabDefinition> tabs = new ArrayList<>();
    protected int currentTab = 0;
    
    protected BaseTabScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    protected void addTab(String key, Runnable onSelect) {
        tabs.add(new TabDefinition(key, onSelect));
    }

    @Override
    protected void init() {
        super.init();
        if (tabNavigation == null) {
            tabNavigation = new TabNavigationWidget(this.width / 2 - 230, 60, 460, 20, tabs, currentTab, this::selectTab);
        } else {
            tabNavigation.updatePosition(this.width / 2 - 230, 60, 460);
        }
        this.addDrawableChild(tabNavigation);
    }

    protected void selectTab(int index) {
        if (index >= 0 && index < tabs.size()) {
            currentTab = index;
            tabs.get(index).onSelect.run();
        }
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(this.parent);
    }

    public record TabDefinition(String key, Runnable onSelect) {}
}