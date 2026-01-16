package net.bichal.bplb.mixin;

import net.bichal.bplb.client.Hud;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SuppressWarnings("unused") @Mixin(Screen.class)
public class TooltipMixin {
    @ModifyVariable(method = "renderWithTooltip", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private int adjustTooltipY(int y) {
        if (Hud.shouldApplyHudOffset() && Constants.CONFIG.isApplyHotbarOffset()) {
            return y + Hud.getCurrentHudOffset();
        }
        return y;
    }
}