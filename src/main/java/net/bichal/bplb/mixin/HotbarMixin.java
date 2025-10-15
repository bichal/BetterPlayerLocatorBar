package net.bichal.bplb.mixin;

import net.bichal.bplb.client.Hud;
import net.bichal.bplb.client.Keybinds;
import net.bichal.bplb.util.MathUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.bichal.bplb.util.Constants.CONFIG;

@Mixin(InGameHud.class)
public class HotbarMixin {
    @Unique
    private static final float BASE_EXPERIENCE_OFFSET = -5;
    @Unique
    private static final int TAB_OFFSET = (int) -(18 * CONFIG.getNameplateScale());
    @Unique
    private float experienceYOffset = 0;
    @Unique
    private float statusYOffset = 0;
    @Unique
    private static long lastPlayerVisibleTime = 0;


    @Inject(method = "renderExperienceLevel", at = @At("HEAD"))
    private void adjustExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        experienceYOffset = updateYOffset(true, experienceYOffset);
        applyTranslation(context, experienceYOffset + 1);
    }

    @Inject(method = "renderExperienceLevel", at = @At("RETURN"))
    private void resetExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void adjustStatusBars(DrawContext context, CallbackInfo ci) {
        statusYOffset = updateYOffset(false, statusYOffset);
        applyTranslation(context, statusYOffset);
    }

    @Inject(method = "renderStatusBars", at = @At("RETURN"))
    private void resetStatusBars(DrawContext context, CallbackInfo ci) {
        context.getMatrices().pop();
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
            targetOffset += TAB_OFFSET;
        }

        float delta = targetOffset - currentOffset;
        return currentOffset + delta * MathUtils.easeInOutQuad(t);
    }

    @Unique
    private void applyTranslation(DrawContext context, float offset) {
        context.getMatrices().push();
        context.getMatrices().translate(0, offset, 0);
    }
}
