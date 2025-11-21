package net.bichal.bplb.gui.widget.entry;

import net.bichal.bplb.gui.Config;
import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.gui.widget.CompactButton;
import net.bichal.bplb.gui.widget.DotPreviewWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class WaypointEntry extends BaseConfigEntry {
    private final UUID uuid;
    private final Config.PlayerAppearance appearance;
    private final Transition expandTransition = Constants.createTransition();
    private final Transition flashTransition = Constants.createFlashTransition();
    private final Consumer<Action> actionHandler;
    private final WaypointType type;
    private final List<CompactButton> mainButtons = new ArrayList<>(2);
    private final List<CompactButton> detailButtons = new ArrayList<>(5);
    private boolean expanded = false;
    private boolean visible = true;
    private boolean customEnabled = true;
    private long flashStartTime = -1;

    public WaypointEntry(MinecraftClient client, String name, UUID uuid, Config.PlayerAppearance appearance,
                         WaypointType type, Consumer<Action> actionHandler) {
        super(client, name, Text.literal(name));
        this.uuid = uuid;
        this.appearance = appearance;
        this.type = type;
        this.actionHandler = actionHandler;
        initButtons();
    }

    private void initButtons() {
        mainButtons.add(new CompactButton(0, 0, 20, 20, Text.literal(visible ? "👁" : "🚫"), b -> {
            visible = !visible;
            b.setMessage(Text.literal(visible ? "👁" : "🚫"));
            actionHandler.accept(Action.TOGGLE_VISIBILITY);
        }));

        if (type != WaypointType.DEATH_MARKER) {
            mainButtons.add(new CompactButton(0, 0, 20, 20, Text.literal("✎"), b -> {
                expanded = !expanded;
                actionHandler.accept(Action.TOGGLE_EXPAND);
            }));
        } else {
            mainButtons.add(new CompactButton(0, 0, 20, 20, Text.literal("🗑"), b -> {
                actionHandler.accept(Action.DELETE);
            }));
        }

        if (type != WaypointType.DEATH_MARKER) {
            detailButtons.add(new CompactButton(0, 0, 20, 20, Text.literal(customEnabled ? "✓" : "✗"), b -> {
                customEnabled = !customEnabled;
                b.setMessage(Text.literal(customEnabled ? "✓" : "✗"));
                actionHandler.accept(Action.TOGGLE_CUSTOM);
            }));
            detailButtons.add(new CompactButton(0, 0, 20, 20, Text.literal("🗑"), b -> actionHandler.accept(Action.DELETE)));
            detailButtons.add(new CompactButton(0, 0, 20, 20, Text.literal("📋"), b -> actionHandler.accept(Action.DUPLICATE)));
            detailButtons.add(new CompactButton(0, 0, 20, 20, Text.literal("▲"), b -> actionHandler.accept(Action.MOVE_UP)));
            detailButtons.add(new CompactButton(0, 0, 20, 20, Text.literal("▼"), b -> actionHandler.accept(Action.MOVE_DOWN)));
        }
    }

    public void triggerFlash() {
        flashStartTime = System.currentTimeMillis();
        flashTransition.setTarget(1f);
    }

    @Override
    protected void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        if (flashStartTime > 0) {
            long elapsed = System.currentTimeMillis() - flashStartTime;
            if (elapsed > Constants.HIGHLIGHT_DURATION_MS) {
                flashStartTime = -1;
                flashTransition.setTarget(0f);
            } else {
                float progress = 1f - (elapsed / (float) Constants.HIGHLIGHT_DURATION_MS);
                float pulse = (float) Math.sin(elapsed * 0.008) * 0.5f + 0.5f;
                int flashAlpha = (int) (pulse * progress * 96);
                context.fill(x + 4, y, x + width - 4, y + height, Constants.COLOR_FLASH_OVERLAY | (flashAlpha << 24));
            }
        }

        DotPreviewWidget.render(context, x + 10, y + (height - 9) / 2, appearance, label.getString(), uuid);
        context.drawText(client.textRenderer, label, x + 25, y + (height - 8) / 2, 0xFFFFFF, true);

        int buttonX = x + width - 10;
        for (int i = mainButtons.size() - 1; i >= 0; i--) {
            buttonX -= 24;
            CompactButton btn = mainButtons.get(i);
            btn.setX(buttonX);
            btn.setY(y + (height - 20) / 2);
            btn.render(context, mouseX, mouseY, delta);
        }

        if (expanded && type != WaypointType.DEATH_MARKER) {
            expandTransition.setTarget(1f);
            float progress = expandTransition.update();
            if (progress > 0.01f) {
                renderExpandedContent(context, x, y + height, width, mouseX, mouseY, delta, progress);
            }
        } else {
            expandTransition.setTarget(0f);
            expandTransition.update();
        }
    }

    private void renderExpandedContent(DrawContext context, int x, int y, int width, int mouseX, int mouseY, float delta, float alpha) {
        int expandedHeight = 80;
        int actualHeight = (int) (expandedHeight * alpha);
        int bgAlpha = (int) (alpha * 200);

        context.fill(x + 20, y, x + width - 20, y + actualHeight, (bgAlpha << 24));
        context.drawBorder(x + 20, y, width - 40, actualHeight, 0xFF404040);

        if (alpha > 0.5f) {
            int detailY = y + 8;
            context.drawText(client.textRenderer, Text.literal("Custom Settings:"), x + 30, detailY, 0xFFFFFF, true);

            int btnX = x + width - 30;
            for (int i = detailButtons.size() - 1; i >= 0; i--) {
                btnX -= 24;
                CompactButton btn = detailButtons.get(i);
                btn.setX(btnX);
                btn.setY(detailY - 2);
                btn.render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CompactButton btn : mainButtons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) return true;
        }
        if (expanded) {
            for (CompactButton btn : detailButtons) {
                if (btn.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }

    @Override
    public List<? extends Element> children() {
        List<Element> list = new ArrayList<>(mainButtons);
        if (expanded) list.addAll(detailButtons);
        return list;
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return children().stream()
                .filter(e -> e instanceof Selectable)
                .map(e -> (Selectable) e)
                .toList();
    }

    public enum WaypointType {
        PLAYER, DEATH_MARKER, LODESTONE
    }

    public enum Action {
        TOGGLE_VISIBILITY, TOGGLE_EXPAND, TOGGLE_CUSTOM, DELETE, DUPLICATE, MOVE_UP, MOVE_DOWN
    }
}