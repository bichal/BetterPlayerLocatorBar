package net.bichal.bplb.mixin;

import net.bichal.bplb.client.Client;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the mod's icons in place of the vanilla dots; the vanilla bar background stays. */
@Mixin(LocatorBarRenderer.class)
public class LocatorBarMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void bplb$render(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (!Constants.CONFIG.isModEnabled()) return;
        Client.onHudRender(context);
        ci.cancel();
    }
}
