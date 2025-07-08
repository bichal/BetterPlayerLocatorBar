package net.bichal.bplb.config.widget.entries;

import net.bichal.bplb.config.widget.CustomSliderWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class IntegerSliderOptionEntry extends AbstractSliderOptionEntry {
    public IntegerSliderOptionEntry(MinecraftClient client, String key, int initialValue, int min, int max, Consumer<Integer> valueConsumer, Runnable onDirty) {
        super(client, key, new CustomSliderWidget(0, 0, Constants.CONFIG_SLIDER_WIDTH, 20, Text.empty(), MathHelper.clamp((float) (initialValue - min) / (max - min), 0.0f, 1.0f)) {
            @Override public void updateMessage() {
                int value = min + (int) Math.round(this.value * (max - min));
                this.setMessage(Text.literal(String.valueOf(value)));
            }

            @Override protected void applyValue() {
                int value = min + (int) Math.round(this.value * (max - min));
                valueConsumer.accept(value);
                onDirty.run();
            }
        });
        this.slider.updateMessage();
    }
}
