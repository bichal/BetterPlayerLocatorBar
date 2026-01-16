package net.bichal.bplb.gui.widget.entry;

import net.bichal.bplb.gui.config.SearchEngine;
import net.bichal.bplb.gui.widget.CompactButton;
import net.bichal.bplb.util.ColorConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public final class SearchResultEntry extends BaseConfigEntry {
    private final CompactButton jumpButton;
    private final Text highlightedLabel;

    public SearchResultEntry(MinecraftClient client, SearchEngine.SearchResult result,
                             Text highlightedLabel, Consumer<SearchEngine.SearchResult> onJump) {
        super(client, result.key(), result.label());
        this.highlightedLabel = highlightedLabel;
        this.jumpButton = CompactButton.text(0, 0, 30, 18, Text.literal("→"),
                b -> onJump.accept(result));
    }

    @Override
    protected void renderContent(DrawContext context, int x, int y, int width, int height,
                                 int mouseX, int mouseY, float delta) {
        context.drawText(client.textRenderer, highlightedLabel, x + 10,
                y + (height - 8) / 2, ColorConstants.COLOR_WHITE, true);

        jumpButton.setX(x + width - 40);
        jumpButton.setY(y + (height - 18) / 2);
        jumpButton.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return jumpButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {return List.of(jumpButton);}

    @Override
    public List<? extends Element> children() {return List.of(jumpButton);}
}