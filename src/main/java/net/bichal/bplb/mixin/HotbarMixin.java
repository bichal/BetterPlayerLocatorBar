package net.bichal.bplb.mixin;

import net.bichal.bplb.client.Hud;
import net.bichal.bplb.client.Keybinds;
import net.bichal.bplb.util.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.bichal.bplb.util.Constants.CONFIG;

@Mixin(Gui.class)
public class HotbarMixin {
    @Unique
    private static final float BASE_EXPERIENCE_OFFSET = -5;
    @Unique
    private static int tabOffset() {
        return (int) -(18 * CONFIG.getNameplateScale());
    }
    @Unique
    private float experienceYOffset = 0;
    @Unique
    private float statusYOffset = 0;
    @Unique
    private static long lastPlayerVisibleTime = 0;


    @Inject(method = "renderExperienceLevel", at = @At("HEAD"))
    private void adjustExperienceLevel(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        experienceYOffset = updateYOffset(true, experienceYOffset);
        applyTranslation(context, experienceYOffset + 1);
    }

    @Inject(method = "renderExperienceLevel", at = @At("RETURN"))
    private void resetExperienceLevel(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        context.pose().popPose();
    }

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"))
    private void adjustStatusBars(GuiGraphics context, CallbackInfo ci) {
        statusYOffset = updateYOffset(false, statusYOffset);
        applyTranslation(context, statusYOffset);
    }

    @Inject(method = "renderPlayerHealth", at = @At("RETURN"))
    private void resetStatusBars(GuiGraphics context, CallbackInfo ci) {
        context.pose().popPose();
    }

    @Unique
    private float updateYOffset(boolean isExperience, float currentOffset) {
        float t = Math.min(CONFIG.getLerpSpeed() * 0.5f, 1.0f);
        float defaultYOffset = -1;

        if (!CONFIG.isModEnabled()) {
            float targetOffset = 0;
            float delta = targetOffset - currentOffset;
            return currentOffset + delta * MathUtils.easeInOutQuad(t);
        }

        boolean shouldOffset = Hud.shouldApplyHudOffset();
        long currentTime = System.currentTimeMillis();

        if (shouldOffset) {
            lastPlayerVisibleTime = currentTime;
        }

        boolean recentlyVisible = (currentTime - lastPlayerVisibleTime) < 3000;

        if (!shouldOffset) {
            float targetOffset;
            if (recentlyVisible) {
                targetOffset = isExperience ? BASE_EXPERIENCE_OFFSET : defaultYOffset;
            } else {
                targetOffset = 0;
            }
            float delta = targetOffset - currentOffset;
            return currentOffset + delta * MathUtils.easeInOutQuad(t);
        }

        if (!CONFIG.isApplyHotbarOffset()) {
            float targetOffset = isExperience ? BASE_EXPERIENCE_OFFSET : defaultYOffset;
            float delta = targetOffset - currentOffset;
            return currentOffset + delta * MathUtils.easeInOutQuad(t);
        }

        int targetOffset = (int) (isExperience ? BASE_EXPERIENCE_OFFSET : defaultYOffset);
        boolean shouldShowNames = Keybinds.shouldShowPlayerNames() || CONFIG.isAlwaysShowPlayerNames();
        if (shouldShowNames) {
            targetOffset += tabOffset();
        }

        float delta = targetOffset - currentOffset;
        return currentOffset + delta * MathUtils.easeInOutQuad(t);
    }

    @Unique
    private void applyTranslation(GuiGraphics context, float offset) {
        context.pose().pushPose();
        context.pose().translate(0, offset, 0);
    }
}
