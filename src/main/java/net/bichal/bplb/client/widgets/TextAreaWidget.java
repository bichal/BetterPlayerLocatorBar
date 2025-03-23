package net.bichal.bplb.client.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class TextAreaWidget extends ClickableWidget {
    private final TextRenderer textRenderer;
    private String text = "";
    private String[] lines = new String[0];
    private boolean focused = false;
    private int cursorPos = 0;
    private int selectionStart = 0;
    private int selectionEnd = 0;
    private int cursorLine = 0;
    private int cursorColumn = 0;
    private int scrollOffset = 0;
    private float zoomFactor = 1.0f;
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 2.0f;
    private final List<String> undoHistory = new ArrayList<>();
    private final List<String> redoHistory = new ArrayList<>();

    public TextAreaWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
        saveToUndoHistory();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        context.getMatrices().scale(zoomFactor, zoomFactor, 1.0f);

        int padding = 4;
        int lineHeight = 8;
        int visibleLines = (int) (height / (lineHeight * zoomFactor));

        for (int i = 0; i < visibleLines && i + scrollOffset < lines.length; i++) {
            String line = lines[i + scrollOffset];
            context.drawText(this.textRenderer, line, (int) (this.getX() / zoomFactor) + padding, (int) (this.getY() / zoomFactor) + padding + (i * lineHeight), 0xFFFFFF, false);
        }

        if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cursorX = (int) (this.getX() / zoomFactor) + padding + textRenderer.getWidth(lines[cursorLine].substring(0, cursorColumn));
            int cursorY = (int) (this.getY() / zoomFactor) + padding + ((cursorLine - scrollOffset) * lineHeight);
            context.fill(cursorX, cursorY, cursorX + 1, cursorY + (int) (lineHeight / zoomFactor), 0xFFFFFFFF);
        }

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.focused = true;

            int relativeX = (int) ((mouseX - this.getX()) / zoomFactor);
            int relativeY = (int) ((mouseY - this.getY()) / zoomFactor) + (scrollOffset * 16);
            cursorLine = MathHelper.clamp(relativeY / 16, 0, lines.length - 1);
            cursorColumn = MathHelper.clamp(textRenderer.trimToWidth(lines[cursorLine], relativeX).length(), 0, lines[cursorLine].length());

            cursorPos = 0;
            for (int i = 0; i < cursorLine; i++) {
                cursorPos += lines[i].length() + 1;
            }
            cursorPos += cursorColumn;

            return true;
        } else {
            this.focused = false;
            return false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.focused) {
            boolean ctrlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;

            switch (keyCode) {
                case GLFW.GLFW_KEY_LEFT:
                    if (ctrlPressed) moveCursorToPreviousWord();
                    else moveCursorLeft();
                    return true;
                case GLFW.GLFW_KEY_RIGHT:
                    if (ctrlPressed) moveCursorToNextWord();
                    else moveCursorRight();
                    return true;
                case GLFW.GLFW_KEY_UP:
                    moveCursorUp();
                    return true;
                case GLFW.GLFW_KEY_DOWN:
                    moveCursorDown();
                    return true;
                case GLFW.GLFW_KEY_HOME:
                    moveCursorToStartOfLine();
                    return true;
                case GLFW.GLFW_KEY_END:
                    moveCursorToEndOfLine();
                    return true;
                case GLFW.GLFW_KEY_BACKSPACE:
                    handleBackspace();
                    return true;
                case GLFW.GLFW_KEY_DELETE:
                    handleDelete();
                    return true;
                case GLFW.GLFW_KEY_EQUAL:
                    if (ctrlPressed) adjustZoom(0.1f);
                    return true;
                case GLFW.GLFW_KEY_MINUS:
                    if (ctrlPressed) adjustZoom(-0.1f);
                    return true;
                case GLFW.GLFW_KEY_Z:
                    if (ctrlPressed) undo();
                    return true;
                case GLFW.GLFW_KEY_Y:
                    if (ctrlPressed) redo();
                    return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!this.focused) return false;
        insertText(String.valueOf(chr));
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.isMouseOver(mouseX, mouseY)) {
            scrollOffset = MathHelper.clamp(scrollOffset - (int) verticalAmount, 0, Math.max(0, lines.length - (int) (height / (16 * zoomFactor))));
            return true;
        }
        return false;
    }

    private void adjustZoom(float delta) {
        float newZoom = MathHelper.clamp(zoomFactor + delta, MIN_ZOOM, MAX_ZOOM);
        if (newZoom != zoomFactor) {
            zoomFactor = newZoom;
            cursorPos = MathHelper.clamp(cursorPos, 0, text.length());
            selectionStart = MathHelper.clamp(selectionStart, 0, text.length());
            selectionEnd = MathHelper.clamp(selectionEnd, 0, text.length());
        }
    }

    private void moveCursorLeft() {
        if (cursorPos > 0) {
            cursorPos--;
            updateCursorLineAndColumn();
        }
    }

    private void moveCursorRight() {
        if (cursorPos < text.length()) {
            cursorPos++;
            updateCursorLineAndColumn();
        }
    }

    private void moveCursorUp() {
        if (cursorLine > 0) {
            cursorLine--;
            cursorColumn = Math.min(cursorColumn, lines[cursorLine].length());
            updateCursorPos();
        }
    }

    private void moveCursorDown() {
        if (cursorLine < lines.length - 1) {
            cursorLine++;
            cursorColumn = Math.min(cursorColumn, lines[cursorLine].length());
            updateCursorPos();
        }
    }

    private void moveCursorToStartOfLine() {
        cursorColumn = 0;
        updateCursorPos();
    }

    private void moveCursorToEndOfLine() {
        cursorColumn = lines[cursorLine].length();
        updateCursorPos();
    }

    private void moveCursorToPreviousWord() {
        if (cursorPos > 0) {
            while (cursorPos > 0 && Character.isWhitespace(text.charAt(cursorPos - 1))) {
                cursorPos--;
            }
            while (cursorPos > 0 && !Character.isWhitespace(text.charAt(cursorPos - 1))) {
                cursorPos--;
            }
            updateCursorLineAndColumn();
        }
    }

    private void moveCursorToNextWord() {
        if (cursorPos < text.length()) {
            while (cursorPos < text.length() && Character.isWhitespace(text.charAt(cursorPos))) {
                cursorPos++;
            }
            while (cursorPos < text.length() && !Character.isWhitespace(text.charAt(cursorPos))) {
                cursorPos++;
            }
            updateCursorLineAndColumn();
        }
    }

    private void handleBackspace() {
        if (selectionStart != selectionEnd) {
            String newText = text.substring(0, selectionStart) + text.substring(selectionEnd);
            setText(newText);
            cursorPos = selectionStart;
        } else if (cursorPos > 0) {
            String newText = text.substring(0, cursorPos - 1) + text.substring(cursorPos);
            setText(newText);
            cursorPos--;
        }
    }

    private void handleDelete() {
        if (selectionStart != selectionEnd) {
            String newText = text.substring(0, selectionStart) + text.substring(selectionEnd);
            setText(newText);
            cursorPos = selectionStart;
        } else if (cursorPos < text.length()) {
            String newText = text.substring(0, cursorPos) + text.substring(cursorPos + 1);
            setText(newText);
        }
    }

    private void insertText(String text) {
        String newText = new StringBuilder(this.text).insert(cursorPos, text).toString();
        if (newText.length() <= 32500) {
            setText(newText);
            cursorPos += text.length();
            updateCursorLineAndColumn();
        }
    }

    private void updateCursorLineAndColumn() {
        cursorLine = 0;
        cursorColumn = cursorPos;
        for (String line : lines) {
            if (cursorColumn > line.length()) {
                cursorColumn -= line.length() + 1;
                cursorLine++;
            } else {
                break;
            }
        }
    }

    private void updateCursorPos() {
        cursorPos = 0;
        for (int i = 0; i < cursorLine; i++) {
            cursorPos += lines[i].length() + 1;
        }
        cursorPos += cursorColumn;
    }

    public void setText(String text) {
        this.text = text;
        this.lines = text.split("\n");
        updateCursorLineAndColumn();
    }

    public String getText() {
        return this.text;
    }

    private void saveToUndoHistory() {
        undoHistory.add(this.text);
        if (undoHistory.size() > 50) {
            undoHistory.removeFirst();
        }
        redoHistory.clear();
    }

    private void undo() {
        if (undoHistory.size() > 1) {
            redoHistory.add(undoHistory.removeLast());
            setText(undoHistory.getLast());
        }
    }

    private void redo() {
        if (!redoHistory.isEmpty()) {
            undoHistory.add(redoHistory.removeLast());
            setText(undoHistory.getLast());
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
