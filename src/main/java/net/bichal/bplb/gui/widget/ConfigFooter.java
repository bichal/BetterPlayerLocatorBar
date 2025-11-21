package net.bichal.bplb.gui.widget;

import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.gui.config.ConfigState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public final class ConfigFooter extends AnimatedWidget {
    private final CompactButton undoBtn, redoBtn, doneBtn, applyBtn, cancelBtn, resetBtn;
    private final ConfigState state;
    private final Transition[] buttonHovers = new Transition[6];

    public ConfigFooter(int y, int width, ConfigState state, Consumer<Action> actionHandler) {
        super(0, y, width, 30, Text.empty());
        this.state = state;

        int buttonWidth = 80;
        int spacing = 6;
        int totalWidth = buttonWidth * 6 + spacing * 5;
        int startX = (width - totalWidth) / 2;

        undoBtn = new CompactButton(startX, y + 5, buttonWidth, 20, Text.translatable("bplb.config.undo"),
                b -> actionHandler.accept(Action.UNDO));
        redoBtn = new CompactButton(startX + (buttonWidth + spacing), y + 5, buttonWidth, 20,
                Text.translatable("bplb.config.redo"), b -> actionHandler.accept(Action.REDO));
        doneBtn = new CompactButton(startX + (buttonWidth + spacing) * 2, y + 5, buttonWidth, 20,
                Text.translatable("gui.done"), b -> actionHandler.accept(Action.DONE));
        applyBtn = new CompactButton(startX + (buttonWidth + spacing) * 3, y + 5, buttonWidth, 20,
                Text.translatable("bplb.config.apply"), b -> actionHandler.accept(Action.APPLY));
        cancelBtn = new CompactButton(startX + (buttonWidth + spacing) * 4, y + 5, buttonWidth, 20,
                Text.translatable("gui.cancel"), b -> actionHandler.accept(Action.CANCEL));
        resetBtn = new CompactButton(startX + (buttonWidth + spacing) * 5, y + 5, buttonWidth, 20,
                Text.translatable("bplb.config.reset"), b -> actionHandler.accept(Action.RESET));

        for (int i = 0; i < 6; i++) {
            buttonHovers[i] = new Transition(0, 15f, 8f);
        }
    }

    public void updateButtons() {
        undoBtn.active = state.canUndo();
        redoBtn.active = state.canRedo();
        doneBtn.active = !state.isDirty();
        applyBtn.active = state.isDirty();
        resetBtn.active = Screen.hasShiftDown();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, getY(), width, getY() + height, 0xD0000000);
        context.drawHorizontalLine(0, width, getY(), 0xFF404040);

        updateButtons();

        undoBtn.render(context, mouseX, mouseY, delta);
        redoBtn.render(context, mouseX, mouseY, delta);
        doneBtn.render(context, mouseX, mouseY, delta);
        applyBtn.render(context, mouseX, mouseY, delta);
        cancelBtn.render(context, mouseX, mouseY, delta);
        resetBtn.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return undoBtn.mouseClicked(mouseX, mouseY, button) ||
                redoBtn.mouseClicked(mouseX, mouseY, button) ||
                doneBtn.mouseClicked(mouseX, mouseY, button) ||
                applyBtn.mouseClicked(mouseX, mouseY, button) ||
                cancelBtn.mouseClicked(mouseX, mouseY, button) ||
                resetBtn.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {}

    public enum Action {UNDO, REDO, DONE, APPLY, CANCEL, RESET}
}