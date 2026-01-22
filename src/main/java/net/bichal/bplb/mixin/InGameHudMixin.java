package net.bichal.bplb.mixin;

import net.bichal.bichalutils.util.MathUtil;
import net.bichal.bplb.client.Keybinds;
import net.bichal.bplb.client.ModConfig;
import net.bichal.bplb.client.gui.Hud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique private static float experienceYOffset = 0;
    @Unique private static float statusYOffset = 0;
    @Unique private static long lastPlayerVisibleTime = 0;
    @Unique private static final int MAX_OFFSET_THRESHOLD = 40;

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"))
    private void adjustExperienceLevel(DrawContext context, float x, CallbackInfo ci) {
        if (shouldFreeze()) return;
        experienceYOffset = updateYOffset(true, experienceYOffset);
        context.getMatrices().push();
        context.getMatrices().translate(0, experienceYOffset + 1, 0);
    }

    @Inject(method = "renderExperienceLevel", at = @At("RETURN"))
    private void resetExperienceLevel(DrawContext context, float x, CallbackInfo ci) {
        if (shouldFreeze()) return;
        context.getMatrices().pop();
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void adjustStatusBars(DrawContext context, CallbackInfo ci) {
        if (shouldFreeze()) return;
        statusYOffset = updateYOffset(false, statusYOffset);
        context.getMatrices().push();
        context.getMatrices().translate(0, statusYOffset, 0);
    }

    @Inject(method = "renderStatusBars", at = @At("RETURN"))
    private void resetStatusBars(DrawContext context, CallbackInfo ci) {
        if (shouldFreeze()) return;
        context.getMatrices().pop();
    }

    @Unique
    private static boolean shouldFreeze() {
        return ModConfig.globalHudYOffset < -MAX_OFFSET_THRESHOLD;
    }

    @Unique
    private static float updateYOffset(boolean isExperience, float currentOffset) {
        float t = Math.min(ModConfig.lerpSpeed * 0.5f, 1.0f);
        int globalOffset = ModConfig.globalHudYOffset;

        if (!ModConfig.modEnabled) {
            float delta = globalOffset - currentOffset;
            return currentOffset + delta * MathUtil.easeInOutQuad(t);
        }

        boolean shouldOffset = Hud.shouldApplyHudOffset();
        long currentTime = System.currentTimeMillis();
        
        if (shouldOffset) lastPlayerVisibleTime = currentTime;
        
        boolean recentlyVisible = (currentTime - lastPlayerVisibleTime) < 3000;
        boolean showingTab = Keybinds.shouldShowPlayerNames();

        if (!shouldOffset && !recentlyVisible && !showingTab) {
            float delta = globalOffset - currentOffset;
            return currentOffset + delta * MathUtil.easeInOutQuad(t);
        }

        if (!ModConfig.applyHotbarOffset && !showingTab) {
            float targetOffset = isExperience ? -5 + globalOffset : globalOffset - 1;
            float delta = targetOffset - currentOffset;
            return currentOffset + delta * MathUtil.easeInOutQuad(t);
        }

        int baseOffset = isExperience ? -5 + globalOffset : globalOffset - 1;
        
        if (showingTab && baseOffset <= 0) {
            baseOffset -= (18 + (int) (12 * ModConfig.nameplateScale));
        }

        float delta = baseOffset - currentOffset;
        float result = currentOffset + delta * MathUtil.easeInOutQuad(t);
        
        if (isExperience) {
            Hud.setCurrentHudOffset((int)result);
        }
        
        return result;
    }
}
