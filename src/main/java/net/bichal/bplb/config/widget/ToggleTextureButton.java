package net.bichal.bplb.config.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@SuppressWarnings("unused") // Future Implementation
public class ToggleTextureButton extends AnimatedWidget {
    private final Identifier onTexture;
    private final Identifier offTexture;
    private final PressAction onPress;
    private boolean state;

    public ToggleTextureButton(int x, int y, int width, int height, Identifier onTexture, Identifier offTexture, boolean initialState, PressAction onPress) {
        super(x, y, width, height, Text.empty());
        this.onTexture = onTexture;
        this.offTexture = offTexture;
        this.state = initialState;
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        updateHoverAnimation(mouseX, mouseY, 0.2f);

        int borderColor = getBorderColor();
        context.drawBorder(this.getX(), this.getY(), this.width, this.height, borderColor);

        Identifier texture = state ? onTexture : offTexture;
        context.drawTexture(RenderLayer::getGuiTextured, texture, this.getX(), this.getY(), 0, 0, 20, 20, 20, 20, 20, 20);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.state = !this.state;
        this.onPress.onPress(this);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    public interface PressAction {
        void onPress(ToggleTextureButton button);
    }
}
