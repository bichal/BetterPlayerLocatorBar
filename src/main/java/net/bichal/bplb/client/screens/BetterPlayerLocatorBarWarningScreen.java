package net.bichal.bplb.client.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class BetterPlayerLocatorBarWarningScreen extends Screen {
    private final Screen parent;

    public BetterPlayerLocatorBarWarningScreen(Screen parent) {
        super(Text.translatable("screen.bplb.warning.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonSpacing = 10;
        int buttonsY = height / 2 + 50;

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bplb.warning.continue"), button -> Objects.requireNonNull(client).setScreen(new BetterPlayerLocatorBarJsonEditorScreen(parent))).dimensions(width / 2 - buttonWidth - buttonSpacing / 2, buttonsY, buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bplb.warning.back"), button -> Objects.requireNonNull(client).setScreen(parent)).dimensions(width / 2 + buttonSpacing / 2, buttonsY, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.fill(width / 2 - 170, height / 2 - 80, width / 2 + 170, height / 2 + 80, 0xC0FF2020);
        context.fill(width / 2 - 168, height / 2 - 78, width / 2 + 168, height / 2 + 78, 0xC0000000);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("screen.bplb.warning.title").setStyle(Style.EMPTY.withBold(true).withColor(Formatting.RED)),
                this.width / 2,
                height / 2 - 65,
                0xFFFFFF
        );

        Text[] messages = {
                Text.translatable("screen.bplb.warning.line1"),
                Text.translatable("screen.bplb.warning.line2"),
                Text.translatable("screen.bplb.warning.line3"),
                Text.translatable("screen.bplb.warning.line4"),
                Text.translatable("screen.bplb.warning.line5")
        };

        int y = height / 2 - 40;
        for (Text message : messages) {
            context.drawCenteredTextWithShadow(this.textRenderer, message, this.width / 2, y, 0xFFFFFF);
            y += 15;
        }
    }
}
