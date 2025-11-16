package net.bichal.bplb.gui.widget.entries;

import net.bichal.bplb.gui.widget.SliderWidget;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;
import java.util.function.Consumer;

public class ServerDoubleSliderEntry extends AbstractSliderOptionEntry {
    
    public ServerDoubleSliderEntry(MinecraftClient client, String key, double initialValue, double min, double max, Consumer<Double> valueConsumer) {
        super(client, key, createSlider(initialValue, min, max, valueConsumer));
    }
    
    private static SliderWidget createSlider(double initialValue, double min, double max, Consumer<Double> valueConsumer) {
        double range = max - min;
        
        return new SliderWidget(0, 0, Constants.CONFIG_SLIDER_WIDTH, 20, Text.empty(), MathHelper.clamp((initialValue - min) / range, 0.0, 1.0)) {
            @Override
            public void updateMessage() {
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                double value = min + range * this.value;
                this.setMessage(Text.literal(df.format(value)));
            }

            @Override
            protected void applyValue() {
                double value = MathHelper.clamp(min + range * this.value, min, max);
                valueConsumer.accept(value);
            }
        };
    }
}