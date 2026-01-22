package net.bichal.bplb.mixin;

import net.bichal.bplb.client.ModConfig;
import net.bichal.bplb.client.gui.Hud;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SuppressWarnings("unused") @Mixin(Screen.class)
public class TooltipMixin {
    @ModifyVariable(method = "renderWithTooltip", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private int adjustTooltipY(int y) {
        if (Hud.shouldApplyHudOffset() && ModConfig.applyHotbarOffset) {
            return y + Hud.getCurrentHudOffset();
        }
        return y;
    }
}