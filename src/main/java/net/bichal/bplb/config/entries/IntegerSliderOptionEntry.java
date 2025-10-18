package net.bichal.bplb.config.entries;

import net.bichal.bplb.config.widget.SliderWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import java.util.function.Consumer;

public class IntegerSliderOptionEntry extends AbstractSliderOptionEntry {
    public IntegerSliderOptionEntry(MinecraftClient client, String key, int initialValue, int min, int max, Consumer<Integer> valueConsumer, Runnable onDirty) {
        super(client, key, createSlider(initialValue, min, max, valueConsumer, onDirty));
    }

    private static SliderWidget createSlider(int initialValue, int min, int max, Consumer<Integer> valueConsumer, Runnable onDirty) {
        boolean useSteps = (max - min) <= Constants.CONFIG_SLIDER_WIDTH;

        return new SliderWidget(0, 0, Constants.CONFIG_SLIDER_WIDTH, 20, Text.empty(), MathHelper.clamp((float) (initialValue - min) / (max - min), 0.0f, 1.0f)) {
            @Override
            public void updateMessage() {
                int value = min + (int) Math.round(this.value * (max - min));
                this.setMessage(Text.literal(String.valueOf(value)));
            }

            @Override
            protected void applyValue() {
                if (useSteps) {
                    int steps = max - min;
                    int step = Math.round((float) (this.value * steps));
                    this.value = (float) step / steps;
                }
                int value = min + (int) Math.round(this.value * (max - min));
                valueConsumer.accept(value);
                onDirty.run();
            }

            @Override
            protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
                super.onDrag(mouseX, mouseY, deltaX, deltaY);
                if (useSteps) {
                    int steps = max - min;
                    int step = Math.round((float) (this.value * steps));
                    this.value = (float) step / steps;
                }
                updateMessage();
            }
        };
    }
}
