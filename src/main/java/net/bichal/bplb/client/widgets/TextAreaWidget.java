package net.bichal.bplb.client.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class TextAreaWidget extends ClickableWidget {
    private final TextRenderer textRenderer;
    private String text = "";
    private int cursorPos = 0;
    private String[] lines = new String[0];
    private boolean focused = false;

    public TextAreaWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int backgroundColor = this.focused ? 0xFF333333 : 0xFF000000;
        int borderColor = this.isFocused() ? 0xFFFFFFFF : 0xFF707070;

        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, backgroundColor);
        context.drawBorder(this.getX(), this.getY(), this.width, this.height, borderColor);

        int firstRow = 0;
        int padding = 5;
        int visibleLines = 0;

        if (this.lines.length > 0) {
            visibleLines = (this.height - (padding * 2)) / 12;
            for (int i = 0; i < visibleLines && i + firstRow < lines.length; i++) {
                String line = lines[i + firstRow];
                context.drawText(this.textRenderer, line, this.getX() + padding, this.getY() + padding + (i * 12), 0xFFFFFF, false);
            }
        }

        if (this.focused) {
            int cursorLine = 0;
            int cursorColumn = 0;
            int totalChars = 0;

            for (int i = 0; i < lines.length; i++) {
                if (totalChars + lines[i].length() >= cursorPos) {
                    cursorLine = i;
                    cursorColumn = cursorPos - totalChars;
                    break;
                }
                totalChars += lines[i].length() + 1;
            }

            if (cursorLine < firstRow + visibleLines) {
                int cursorX = this.getX() + padding + this.textRenderer.getWidth(lines[cursorLine].substring(0, cursorColumn));
                int cursorY = this.getY() + padding + ((cursorLine - firstRow) * 12);

                if ((System.currentTimeMillis() / 500) % 2 == 0) {
                    context.fill(cursorX, cursorY, cursorX + 1, cursorY + 12, 0xFFFFFFFF);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.focused = true;
            return true;
        } else {
            this.focused = false;
            return false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.focused;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!this.focused) return false;

        return insertText(String.valueOf(chr));
    }

    private boolean insertText(String text) {
        String newText = new StringBuilder(this.text).insert(cursorPos, text).toString();
        int maxLength = 32500;
        if (newText.length() <= maxLength) {
            this.setText(newText);
            this.cursorPos += text.length();
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.getX() && mouseX < this.getX() + this.width &&
                mouseY >= this.getY() && mouseY < this.getY() + this.height;
    }

    public void setText(String text) {
        this.text = text;
        this.lines = text.split("\n");
    }

    public String getText() {
        return this.text;
    }

    public boolean isFocused() {
        return this.focused;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}