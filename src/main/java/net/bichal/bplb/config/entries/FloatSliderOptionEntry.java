package net.bichal.bplb.config.entries;

import net.bichal.bplb.config.widget.SliderWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class FloatSliderOptionEntry extends AbstractSliderOptionEntry {
    public FloatSliderOptionEntry(MinecraftClient client, String key, float initialValue, float min, float max, Consumer<Float> valueConsumer, Runnable onDirty) {
        super(client, key, new SliderWidget(0, 0, Constants.CONFIG_SLIDER_WIDTH, 20, Text.empty(), MathHelper.clamp((initialValue - min) / (max - min), 0.0f, 1.0f)) {
            @Override public void updateMessage() {
                float value = min + (max - min) * (float) this.value;
                this.setMessage(Text.literal(String.format("%.2f", value)));
            }

            @Override protected void applyValue() {
                float value = min + (max - min) * (float) this.value;
                valueConsumer.accept(value);
                onDirty.run();
            }
        });
        this.slider.updateMessage();
    }
}
