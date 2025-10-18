package net.bichal.bplb.config.entries;

import net.bichal.bplb.config.widget.SliderWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;
import java.util.function.Consumer;

public class FloatSliderOptionEntry extends AbstractSliderOptionEntry {
    public FloatSliderOptionEntry(MinecraftClient client, String key, float initialValue, float min, float max, Consumer<Float> valueConsumer, Runnable onDirty) {
        super(client, key, createSlider(initialValue, min, max, valueConsumer, onDirty));
    }

    private static SliderWidget createSlider(float initialValue, float min, float max, Consumer<Float> valueConsumer, Runnable onDirty) {
        int steps = (int) ((max - min) * 20);
        boolean useSteps = steps <= Constants.CONFIG_SLIDER_WIDTH && steps > 0;

        float safeMin = Math.min(min, max);
        float safeMax = Math.max(min, max);
        float range = safeMax - safeMin;
        if (range < 0.01f) {
            range = 0.01f;
            safeMax = safeMin + range;
        }

        final float finalRange = range;
        final float finalSafeMin = safeMin;
        final float finalSafeMax = safeMax;

        return new SliderWidget(0, 0, Constants.CONFIG_SLIDER_WIDTH, 20, Text.empty(), MathHelper.clamp((initialValue - finalSafeMin) / finalRange, 0.0f, 1.0f)) {
            @Override
            public void updateMessage() {
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                float value = finalSafeMin + finalRange * (float) this.value;
                this.setMessage(Text.literal(String.valueOf(df.format(value))));
            }

            @Override
            protected void applyValue() {
                if (useSteps) {
                    int step = Math.round((float) (this.value * steps));
                    this.value = MathHelper.clamp((float) step / steps, 0.0f, 1.0f);
                }
                float value = MathHelper.clamp(finalSafeMin + finalRange * (float) this.value, finalSafeMin, finalSafeMax);
                valueConsumer.accept(value);
                onDirty.run();
            }

            @Override
            protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
                super.onDrag(mouseX, mouseY, deltaX, deltaY);
                if (useSteps) {
                    int step = Math.round((float) (this.value * steps));
                    this.value = MathHelper.clamp((float) step / steps, 0.0f, 1.0f);
                }
                updateMessage();
            }
        };
    }
}
