package net.bichal.bplb.config.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class CustomButtonWidget extends CustomPressableWidget {
    protected static final NarrationSupplier DEFAULT_NARRATION_SUPPLIER = Supplier::get;
    protected final PressAction onPress;
    protected final NarrationSupplier narrationSupplier;

    public static Builder builder(Text message, PressAction onPress) {
        return new Builder(message, onPress);
    }

    protected CustomButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.narrationSupplier = narrationSupplier;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected MutableText getNarrationMessage() {
        return this.narrationSupplier.createNarrationMessage(super::getNarrationMessage);
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Environment(EnvType.CLIENT)
    public static class Builder {
        private final Text message;
        private final PressAction onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private NarrationSupplier narrationSupplier = DEFAULT_NARRATION_SUPPLIER;

        public Builder(Text message, PressAction onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public CustomButtonWidget build() {
            CustomButtonWidget buttonWidget = new CustomButtonWidget(this.x, this.y, this.width, this.height, this.message, this.onPress, this.narrationSupplier);
            buttonWidget.setTooltip(this.tooltip);
            return buttonWidget;
        }
    }

    @Environment(EnvType.CLIENT)
    public interface NarrationSupplier {
        MutableText createNarrationMessage(Supplier<MutableText> textSupplier);
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(CustomButtonWidget button);
    }
}
