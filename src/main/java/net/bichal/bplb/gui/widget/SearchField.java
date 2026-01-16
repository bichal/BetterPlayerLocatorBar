package net.bichal.bplb.gui.widget;

import net.bichal.bplb.gui.animation.Transition;
import net.bichal.bplb.util.ColorConstants;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public final class SearchField extends TextFieldWidget {
    private final Transition focusTransition = Constants.createHoverTransition();
    private final Transition modeTransition = Constants.createHoverTransition();

    public SearchField(net.minecraft.client.font.TextRenderer renderer, int x, int y, int w, int h) {
        super(renderer, x, y, w, h, Text.empty());
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean focused = isFocused();
        focusTransition.setTarget(focused ? 1f : 0f);
        float alpha = focusTransition.update();

        int bgColor = this.isFocused() ? ColorConstants.COLOR_BLACK : ColorConstants.COLOR_DARK;
        int borderColor = focused ? ColorConstants.COLOR_DARK : ColorConstants.COLOR_DARK_DISABLED;

        context.fill(getX(), getY(), getX() + width, getY() + height, bgColor);
        context.drawBorder(getX(), getY(), width, height, borderColor);

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    public Text highlightMatches(Text original, String query) {
        if (query.isEmpty()) return original;

        String text = original.getString();
        String lower = text.toLowerCase();
        String queryLower = query.toLowerCase();

        MutableText result = Text.empty();
        int lastEnd = 0;
        int idx;

        while ((idx = lower.indexOf(queryLower, lastEnd)) != -1) {
            if (idx > lastEnd) {
                result.append(Text.literal(text.substring(lastEnd, idx)));
            }
            result.append(Text.literal(text.substring(idx, idx + query.length()))
                    .setStyle(Style.EMPTY.withColor(ColorConstants.COLOR_HIGHLIGHT).withBold(true)));
            lastEnd = idx + query.length();
        }

        if (lastEnd < text.length()) {
            result.append(Text.literal(text.substring(lastEnd)));
        }

        return result;
    }
}
