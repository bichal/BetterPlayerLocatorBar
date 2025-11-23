package net.bichal.bplb.gui.widget;

import net.bichal.bplb.gui.config.ConfigState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public final class ConfigFooter extends AnimatedWidget {
    private final CompactButton undoBtn, redoBtn, applyBtn, cancelBtn, doneBtn, resetBtn;
    private boolean resetConfirmation = false;
    private long resetClickTime = 0;
    private final ConfigState state;

    public ConfigFooter(int y, int width, ConfigState state, Consumer<Action> actionHandler) {
        super(0, y, width, 30, Text.empty());
        this.state = state;

        int buttonSize = 20;
        int buttonWidth = 80;
        int spacing = 6;
        int totalWidth = (buttonSize * 2) + (buttonWidth * 4) + (spacing * 5);
        int startX = (width - totalWidth) / 2;

        undoBtn = CompactButton.texture(startX, y + 5, buttonSize, 60, 0, b -> actionHandler.accept(Action.UNDO));
        redoBtn = CompactButton.texture(startX + buttonSize + spacing, y + 5, buttonSize, 80, 0, b -> actionHandler.accept(Action.REDO));
        cancelBtn = CompactButton.text(startX + (buttonSize + spacing) * 2, y + 5, buttonWidth, buttonSize, Text.translatable("gui.cancel"), b -> actionHandler.accept(Action.CANCEL));
        applyBtn = CompactButton.text(startX + (buttonSize + spacing) * 2 + buttonWidth + spacing, y + 5, buttonWidth, buttonSize, Text.translatable("bplb.config.apply"), b -> actionHandler.accept(Action.APPLY));
        doneBtn = CompactButton.text(startX + (buttonSize + spacing) * 2 + (buttonWidth + spacing) * 2, y + 5, buttonWidth, buttonSize, Text.translatable("gui.done"), b -> actionHandler.accept(Action.DONE));
        resetBtn = CompactButton.text(startX + (buttonSize + spacing) * 2 + (buttonWidth + spacing) * 3, y + 5, buttonWidth, buttonSize, Text.translatable("bplb.config.reset"), b -> {
            long now = System.currentTimeMillis();
            if (Screen.hasShiftDown() && resetConfirmation && (now - resetClickTime) < 2000) {
                actionHandler.accept(Action.RESET);
                resetConfirmation = false;
            } else if (Screen.hasShiftDown()) {
                resetConfirmation = true;
                resetClickTime = now;
            }
        });
    }

    public void updateButtons() {
        undoBtn.active = state.canUndo();
        redoBtn.active = state.canRedo();
        cancelBtn.active = state.isDirty();
        applyBtn.active = state.isDirty();
        doneBtn.active = !state.isDirty();

        long now = System.currentTimeMillis();
        if (resetConfirmation && (now - resetClickTime) > 2000) {
            resetConfirmation = false;
        }

        resetBtn.active = Screen.hasShiftDown();
        if (resetConfirmation && Screen.hasShiftDown()) {
            resetBtn.setMessage(Text.translatable("bplb.config.reset.confirm").styled(s -> s.withColor(0xFF0000).withBold(true)));
        } else {
            resetBtn.setMessage(Text.translatable("bplb.config.reset"));
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
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
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public enum Action {UNDO, REDO, DONE, APPLY, CANCEL, RESET}
}