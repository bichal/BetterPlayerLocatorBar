package net.bichal.bplb.gui.widget;

import net.bichal.bplb.gui.config.ConfigState;
import net.bichal.bplb.util.ColorConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public final class ConfigFooter extends AnimatedWidget {
    private final CompactButton undoBtn, redoBtn, applyBtn, cancelBtn, resetBtn;
    private boolean resetConfirmation = false;
    private long resetClickTime = 0;
    private final ConfigState state;

    public ConfigFooter(int y, int width, ConfigState state, Consumer<Action> actionHandler) {
        super(0, y, width, 30, Text.empty());
        this.state = state;

        int buttonSize = 20;
        int buttonWidth = 80;
        int spacing = 6;
        int totalWidth = (buttonSize * 2) + (buttonWidth * 3) + (spacing * 4);
        int startX = (width - totalWidth) / 2;
        int centeredY = y + (30 - buttonSize) / 2;

        undoBtn = CompactButton.texture(startX, centeredY, buttonSize, 60, 0, b -> actionHandler.accept(Action.UNDO));
        redoBtn = CompactButton.texture(startX + buttonSize + spacing, centeredY, buttonSize, 80, 0, b -> actionHandler.accept(Action.REDO));
        applyBtn = CompactButton.text(startX + (buttonSize + spacing) * 2, centeredY, buttonWidth, buttonSize, Text.translatable("bplb.config.apply"), b -> actionHandler.accept(Action.APPLY));
        cancelBtn = CompactButton.text(startX + (buttonSize + spacing) * 2 + buttonWidth + spacing, centeredY, buttonWidth, buttonSize, Text.translatable("gui.cancel"), b -> actionHandler.accept(Action.CANCEL));
        resetBtn = CompactButton.text(startX + (buttonSize + spacing) * 2 + (buttonWidth + spacing) * 2, centeredY, buttonWidth, buttonSize, Text.translatable("bplb.config.reset_settings"), b -> {
            long now = System.currentTimeMillis();
            if (net.minecraft.client.gui.screen.Screen.hasShiftDown() && resetConfirmation && (now - resetClickTime) < 2000) {
                actionHandler.accept(Action.RESET);
                resetConfirmation = false;
            } else if (net.minecraft.client.gui.screen.Screen.hasShiftDown()) {
                resetConfirmation = true;
                resetClickTime = now;
            }
        });
    }

    public void updateButtons() {
        undoBtn.active = state.canUndo();
        redoBtn.active = state.canRedo();

        boolean hasRealChanges = state.isDirty();

        if (hasRealChanges) {
            cancelBtn.setMessage(Text.translatable("gui.cancel"));
            applyBtn.active = true;
        } else {
            cancelBtn.setMessage(Text.translatable("gui.done"));
            applyBtn.active = false;
        }

        long now = System.currentTimeMillis();
        if (resetConfirmation && (now - resetClickTime) > 2000) {
            resetConfirmation = false;
        }

        resetBtn.active = net.minecraft.client.gui.screen.Screen.hasShiftDown();
        if (resetConfirmation && net.minecraft.client.gui.screen.Screen.hasShiftDown()) {
            resetBtn.setMessage(Text.translatable("bplb.config.reset_confirm.title").styled(s -> s.withColor(ColorConstants.COLOR_RED).withBold(true)));
        } else {
            resetBtn.setMessage(Text.translatable("bplb.config.reset_settings"));
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        updateButtons();
        undoBtn.render(context, mouseX, mouseY, delta);
        redoBtn.render(context, mouseX, mouseY, delta);
        cancelBtn.render(context, mouseX, mouseY, delta);
        applyBtn.render(context, mouseX, mouseY, delta);
        resetBtn.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return undoBtn.mouseClicked(mouseX, mouseY, button) || redoBtn.mouseClicked(mouseX, mouseY, button) || cancelBtn.mouseClicked(mouseX, mouseY, button) || (state.isDirty() && applyBtn.mouseClicked(mouseX, mouseY, button)) || resetBtn.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public enum Action {UNDO, REDO, DONE, APPLY, CANCEL, RESET}
}
