package net.bichal.bplb.config.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** The row types of the settings screen. */
public final class Rows {
    private static final int CONTROL_WIDTH = 120;
    private static final int PAD = 8;

    private Rows() {
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    /** Draws {@code label} with the characters matching {@code query} highlighted. */
    static void drawLabel(GuiGraphics g, Component label, int x, int y, int color, String query) {
        Font font = font();
        String text = label.getString();
        if (query == null || query.isEmpty()) {
            g.drawString(font, text, x, y, color, false);
            return;
        }
        String lower = text.toLowerCase();
        int at = lower.indexOf(query);
        if (at < 0) {
            g.drawString(font, text, x, y, color, false);
            return;
        }
        String head = text.substring(0, at);
        String hit = text.substring(at, at + query.length());
        String tail = text.substring(at + query.length());
        g.drawString(font, head, x, y, color, false);
        int hx = x + font.width(head);
        g.drawString(font, hit, hx, y, Theme.HIGHLIGHT, false);
        g.drawString(font, tail, hx + font.width(hit), y, color, false);
    }

    private static int textY(int y) {
        return y + (Row.HEIGHT - 8) / 2;
    }

    private static void background(GuiGraphics g, int x, int y, int width, boolean hovered) {
        g.fill(x, y, x + width, y + Row.HEIGHT - 2, hovered ? Theme.ROW_HOVER : Theme.ROW);
    }

    public record Section(Component title) implements Row {
        @Override
        public String key() {
            return null;
        }

        @Override
        public int height() {
            return 20;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
            Font font = font();
            int ty = y + 8;
            g.drawString(font, title, x, ty, Theme.ACCENT, false);
            int lineX = x + font.width(title) + 6;
            g.fill(lineX, ty + 4, x + width, ty + 5, Theme.BORDER);
        }
    }

    /** Anything that has a label on the left and a control on the right. */
    public abstract static class Labeled implements Row {
        protected final String key;
        protected final Component label;
        protected final Supplier<String> query;

        protected Labeled(String key, Component label, Supplier<String> query) {
            this.key = key;
            this.label = label;
            this.query = query;
        }

        @Override
        public String key() {
            return key;
        }

        protected int controlX(int x, int width) {
            return x + width - CONTROL_WIDTH - PAD;
        }

        protected boolean overControl(double mouseX, double mouseY, int x, int y, int width) {
            int cx = controlX(x, width);
            return mouseX >= cx && mouseX < cx + CONTROL_WIDTH && mouseY >= y && mouseY < y + Row.HEIGHT - 2;
        }

        protected abstract boolean enabled();

        protected abstract void drawControl(GuiGraphics g, int cx, int y, int mouseX, int mouseY, boolean hovered);

        @Override
        public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
            background(g, x, y, width, hovered);
            drawLabel(g, label, x + PAD, textY(y), enabled() ? Theme.TEXT : Theme.TEXT_DISABLED, query.get());
            drawControl(g, controlX(x, width), y, mouseX, mouseY, hovered);
        }
    }

    public static final class Toggle extends Labeled {
        private final BooleanSupplier getter;
        private final Consumer<Boolean> setter;

        public Toggle(String key, Component label, Supplier<String> query, BooleanSupplier getter, Consumer<Boolean> setter) {
            super(key, label, query);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        protected boolean enabled() {
            return true;
        }

        @Override
        protected void drawControl(GuiGraphics g, int cx, int y, int mouseX, int mouseY, boolean hovered) {
            boolean on = getter.getAsBoolean();
            int w = 30;
            int h = 12;
            int tx = cx + CONTROL_WIDTH - w;
            int ty = y + (Row.HEIGHT - 2 - h) / 2;
            g.fill(tx, ty, tx + w, ty + h, on ? Theme.ACCENT_DIM : Theme.TRACK);
            g.renderOutline(tx, ty, w, h, on ? Theme.ACCENT : Theme.BORDER);
            int knob = on ? tx + w - h + 2 : tx + 2;
            g.fill(knob, ty + 2, knob + h - 4, ty + h - 2, on ? Theme.TEXT : Theme.TEXT_DIM);
            Component state = Component.translatable(on ? "options.on" : "options.off");
            g.drawString(font(), state, tx - 6 - font().width(state), textY(y), on ? Theme.ACCENT : Theme.TEXT_DIM, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
            if (button != 0 || !overControl(mouseX, mouseY, x, y, width)) return false;
            setter.accept(!getter.getAsBoolean());
            return true;
        }
    }

    /** A slider over a numeric range; the bounds may change while the screen is open. */
    public static final class Slider extends Labeled {
        private final DoubleSupplier getter;
        private final DoubleConsumer setter;
        private final DoubleSupplier min;
        private final DoubleSupplier max;
        private final boolean integer;
        private boolean dragging;

        public Slider(String key, Component label, Supplier<String> query, boolean integer, DoubleSupplier getter, DoubleConsumer setter, DoubleSupplier min, DoubleSupplier max) {
            super(key, label, query);
            this.integer = integer;
            this.getter = getter;
            this.setter = setter;
            this.min = min;
            this.max = max;
        }

        public static Slider ofInt(String key, Component label, Supplier<String> query, int min, int max, java.util.function.IntSupplier getter, IntConsumer setter) {
            return new Slider(key, label, query, true, getter::getAsInt, v -> setter.accept((int) Math.round(v)), () -> min, () -> max);
        }

        public static Slider ofFloat(String key, Component label, Supplier<String> query, DoubleSupplier min, DoubleSupplier max, DoubleSupplier getter, DoubleConsumer setter) {
            return new Slider(key, label, query, false, getter, v -> setter.accept(Math.round(v * 100.0) / 100.0), min, max);
        }

        @Override
        protected boolean enabled() {
            return true;
        }

        private double clamped() {
            return Mth.clamp(getter.getAsDouble(), min.getAsDouble(), max.getAsDouble());
        }

        @Override
        protected void drawControl(GuiGraphics g, int cx, int y, int mouseX, int mouseY, boolean hovered) {
            double lo = min.getAsDouble();
            double hi = max.getAsDouble();
            double t = hi > lo ? (clamped() - lo) / (hi - lo) : 0;
            String value = integer ? String.valueOf((int) Math.round(clamped())) : String.format("%.2f", clamped());
            int valueWidth = 38;
            int trackX = cx;
            int trackW = CONTROL_WIDTH - valueWidth - 4;
            int ty = y + (Row.HEIGHT - 2) / 2 - 2;
            g.fill(trackX, ty, trackX + trackW, ty + 4, Theme.TRACK);
            g.fill(trackX, ty, trackX + (int) (trackW * t), ty + 4, dragging ? Theme.ACCENT : Theme.ACCENT_DIM);
            int kx = trackX + (int) ((trackW - 4) * t);
            g.fill(kx, ty - 3, kx + 4, ty + 7, dragging || hovered ? Theme.ACCENT : Theme.TEXT_DIM);
            Font font = font();
            g.drawString(font, value, cx + CONTROL_WIDTH - font.width(value), textY(y), Theme.TEXT, false);
        }

        private void applyMouse(double mouseX, int x, int width) {
            int trackX = controlX(x, width);
            int trackW = CONTROL_WIDTH - 38 - 4;
            double t = Mth.clamp((mouseX - trackX) / trackW, 0.0, 1.0);
            double lo = min.getAsDouble();
            double hi = max.getAsDouble();
            setter.accept(lo + (hi - lo) * t);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
            if (button != 0 || !overControl(mouseX, mouseY, x, y, width)) return false;
            dragging = true;
            applyMouse(mouseX, x, width);
            return true;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int x, int y, int width) {
            if (!dragging) return false;
            applyMouse(mouseX, x, width);
            return true;
        }

        @Override
        public void mouseReleased() {
            dragging = false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount, int x, int y, int width) {
            if (!overControl(mouseX, mouseY, x, y, width)) return false;
            double step = integer ? 1 : 0.01;
            double next = Mth.clamp(getter.getAsDouble() + Math.signum(amount) * step, min.getAsDouble(), max.getAsDouble());
            setter.accept(next);
            return true;
        }
    }

    /** Steps through a list of values with the arrows or the mouse wheel. */
    public static final class Cycle<T> extends Labeled {
        private final Supplier<T> getter;
        private final Consumer<T> setter;
        private final List<T> options;
        private final Function<T, Component> text;
        private final BooleanSupplier active;

        public Cycle(String key, Component label, Supplier<String> query, List<T> options, Function<T, Component> text, Supplier<T> getter, Consumer<T> setter, BooleanSupplier active) {
            super(key, label, query);
            this.options = options;
            this.text = text;
            this.getter = getter;
            this.setter = setter;
            this.active = active;
        }

        @Override
        protected boolean enabled() {
            return active.getAsBoolean() && options.size() > 1;
        }

        private void step(int delta) {
            if (!enabled()) return;
            int index = Math.max(0, options.indexOf(getter.get()));
            setter.accept(options.get(Math.floorMod(index + delta, options.size())));
        }

        @Override
        protected void drawControl(GuiGraphics g, int cx, int y, int mouseX, int mouseY, boolean hovered) {
            Font font = font();
            int color = enabled() ? Theme.TEXT : Theme.TEXT_DISABLED;
            int arrow = enabled() ? Theme.ACCENT : Theme.TEXT_DISABLED;
            int ty = textY(y);
            g.drawString(font, "<", cx, ty, arrow, false);
            g.drawString(font, ">", cx + CONTROL_WIDTH - font.width(">"), ty, arrow, false);
            Component value = text.apply(getter.get());
            g.drawString(font, value, cx + (CONTROL_WIDTH - font.width(value)) / 2, ty, color, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
            if (!overControl(mouseX, mouseY, x, y, width)) return false;
            boolean left = mouseX < controlX(x, width) + CONTROL_WIDTH / 2.0;
            if (button == 0) step(left ? -1 : 1);
            else if (button == 1) step(left ? 1 : -1);
            else return false;
            return true;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount, int x, int y, int width) {
            if (!overControl(mouseX, mouseY, x, y, width)) return false;
            step(amount > 0 ? -1 : 1);
            return true;
        }
    }

    /** A six digit hex color field with a swatch. */
    public static final class ColorField extends Labeled {
        private final EditBox box;
        private final Supplier<Integer> getter;
        private int color;
        private String lastText;

        public ColorField(String key, Component label, Supplier<String> query, int initial, IntConsumer setter, Runnable changed) {
            super(key, label, query);
            this.getter = () -> color;
            this.color = initial;
            this.lastText = String.format("%06X", initial & 0xFFFFFF);
            this.box = new EditBox(font(), 0, 0, CONTROL_WIDTH - 26, 18, label);
            this.box.setMaxLength(6);
            this.box.setFilter(s -> s.matches("[0-9a-fA-F]{0,6}"));
            this.box.setValue(lastText);
            this.box.setResponder(text -> {
                if (text.length() != 6 || text.equals(lastText)) return;
                lastText = text;
                color = 0xFF000000 | Integer.parseInt(text, 16);
                setter.accept(color);
                changed.run();
            });
        }

        @Override
        protected boolean enabled() {
            return true;
        }

        @Override
        protected void drawControl(GuiGraphics g, int cx, int y, int mouseX, int mouseY, boolean hovered) {
            box.setX(cx);
            box.setY(y + 2);
            box.render(g, mouseX, mouseY, 0f);
            int sx = cx + CONTROL_WIDTH - 20;
            g.fill(sx, y + 2, sx + 20, y + 20, getter.get());
            g.renderOutline(sx, y + 2, 20, 18, Theme.BORDER);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
            return box.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return box.isFocused() && box.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            return box.isFocused() && box.charTyped(chr, modifiers);
        }

        @Override
        public void blur() {
            box.setFocused(false);
        }

        @Override
        public boolean hasFocus() {
            return box.isFocused();
        }
    }

    /** A clickable line; used for the player headers, the add-player row and the reset row. */
    public static final class PlayerHeader implements Row {
        private final String name;
        private final BooleanSupplier expanded;
        private final Runnable toggle;
        private final Runnable remove;
        private final Supplier<String> query;

        public PlayerHeader(String name, Supplier<String> query, BooleanSupplier expanded, Runnable toggle, Runnable remove) {
            this.name = name;
            this.query = query;
            this.expanded = expanded;
            this.toggle = toggle;
            this.remove = remove;
        }

        @Override
        public String key() {
            return "player." + name;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
            background(g, x, y, width, hovered);
            Font font = font();
            g.drawString(font, expanded.getAsBoolean() ? "v" : ">", x + PAD, textY(y), Theme.TEXT_DIM, false);
            drawLabel(g, Component.literal(name), x + PAD + 12, textY(y), Theme.TEXT, query.get());
            int rx = x + width - PAD - 18;
            boolean overRemove = mouseX >= rx && mouseX < rx + 18 && mouseY >= y && mouseY < y + Row.HEIGHT - 2;
            g.fill(rx, y + 2, rx + 18, y + Row.HEIGHT - 4, overRemove ? Theme.DANGER : Theme.TRACK);
            g.drawCenteredString(font, "-", rx + 9, textY(y), Theme.TEXT);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
            if (button != 0) return false;
            int rx = x + width - PAD - 18;
            if (mouseX >= rx && mouseX < rx + 18) remove.run();
            else toggle.run();
            return true;
        }
    }

    public static final class AddPlayer implements Row {
        private final EditBox box;
        private final Consumer<String> add;
        private final Predicate valid;

        /** Decides whether a trimmed name can be added. */
        public interface Predicate {
            boolean test(String name);
        }

        public AddPlayer(Component hint, Predicate valid, Consumer<String> add) {
            this.valid = valid;
            this.add = add;
            this.box = new EditBox(font(), 0, 0, 150, 18, hint);
            this.box.setMaxLength(16);
            this.box.setHint(hint.copy().withStyle(net.minecraft.ChatFormatting.GRAY));
        }

        @Override
        public String key() {
            return "player";
        }

        private boolean canAdd() {
            String name = box.getValue().trim();
            return !name.isEmpty() && valid.test(name);
        }

        private void submit() {
            if (!canAdd()) return;
            String name = box.getValue().trim();
            box.setValue("");
            box.setFocused(false);
            add.accept(name);
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
            box.setX(x + PAD);
            box.setY(y + 2);
            box.render(g, mouseX, mouseY, 0f);
            int bx = x + PAD + 150 + 6;
            boolean can = canAdd();
            boolean over = can && mouseX >= bx && mouseX < bx + 18 && mouseY >= y + 2 && mouseY < y + 20;
            g.fill(bx, y + 2, bx + 18, y + 20, over ? Theme.ACCENT : can ? Theme.ACCENT_DIM : Theme.TRACK);
            g.drawCenteredString(font(), "+", bx + 9, y + 7, can ? Theme.TEXT : Theme.TEXT_DISABLED);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
            int bx = x + PAD + 150 + 6;
            if (button == 0 && mouseX >= bx && mouseX < bx + 18 && mouseY >= y + 2 && mouseY < y + 20) {
                submit();
                return true;
            }
            return box.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!box.isFocused()) return false;
            if (keyCode == 257 || keyCode == 335) {
                submit();
                return true;
            }
            return box.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            return box.isFocused() && box.charTyped(chr, modifiers);
        }

        @Override
        public void blur() {
            box.setFocused(false);
        }

        @Override
        public boolean hasFocus() {
            return box.isFocused();
        }
    }

    /** A button that spans the middle of the list; enabled only while shift is held when {@code guarded}. */
    public static final class Action implements Row {
        private final Component label;
        private final BooleanSupplier enabled;
        private final Runnable run;

        public Action(Component label, BooleanSupplier enabled, Runnable run) {
            this.label = label;
            this.enabled = enabled;
            this.run = run;
        }

        @Override
        public String key() {
            return null;
        }

        private int buttonWidth() {
            return 150;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered) {
            int bw = buttonWidth();
            int bx = x + (width - bw) / 2;
            boolean on = enabled.getAsBoolean();
            boolean over = on && mouseX >= bx && mouseX < bx + bw && mouseY >= y && mouseY < y + Row.HEIGHT - 2;
            g.fill(bx, y + 1, bx + bw, y + Row.HEIGHT - 3, over ? Theme.DANGER : Theme.TRACK);
            g.renderOutline(bx, y + 1, bw, Row.HEIGHT - 4, on ? Theme.DANGER : Theme.BORDER);
            g.drawCenteredString(font(), label, bx + bw / 2, textY(y), on ? Theme.TEXT : Theme.TEXT_DISABLED);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
            int bw = buttonWidth();
            int bx = x + (width - bw) / 2;
            if (button == 0 && enabled.getAsBoolean() && mouseX >= bx && mouseX < bx + bw) {
                run.run();
                return true;
            }
            return false;
        }
    }
}
