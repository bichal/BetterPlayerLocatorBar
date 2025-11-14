package net.bichal.bplb.mixin;

import net.bichal.bichalutils.util.MathUtil;
import net.bichal.bplb.client.Hud;
import net.bichal.bplb.client.Keybinds;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
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
    private void adjustExperienceLevel(DrawContext context, float x, CallbackInfo ci) {
        experienceYOffset = updateYOffset(true, experienceYOffset);
        applyTranslation(context, experienceYOffset + 1);
    }

    @Inject(method = "renderExperienceLevel", at = @At("RETURN"))
    private void resetExperienceLevel(DrawContext context, float x, CallbackInfo ci) {
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
        int globalOffset = CONFIG.getGlobalHudYOffset();

        if (!CONFIG.isModEnabled()) {
            float delta = globalOffset - currentOffset;
            return currentOffset + delta * MathUtil.easeInOutQuad(t);
        }

        boolean shouldOffset = Hud.shouldApplyHudOffset();
        long currentTime = System.currentTimeMillis();

        if (shouldOffset) {
            lastPlayerVisibleTime = currentTime;
        }

        boolean recentlyVisible = (currentTime - lastPlayerVisibleTime) < 3000;

        if (!shouldOffset && !recentlyVisible) {
            float delta = globalOffset - currentOffset;
            return currentOffset + delta * MathUtil.easeInOutQuad(t);
        }

        if (!CONFIG.isApplyHotbarOffset()) {
            float targetOffset = isExperience ? BASE_EXPERIENCE_OFFSET + globalOffset : globalOffset - 1;
            float delta = targetOffset - currentOffset;
            return currentOffset + delta * MathUtil.easeInOutQuad(t);
        }

        int baseOffset = isExperience ? (int)(BASE_EXPERIENCE_OFFSET + globalOffset) : globalOffset - 1;
        boolean shouldShowNames = Keybinds.shouldShowPlayerNames() || CONFIG.isAlwaysShowPlayerNames();

        if (shouldShowNames && baseOffset <= 0) {
            baseOffset += TAB_OFFSET;
        }

        float delta = baseOffset - currentOffset;
        float result = currentOffset + delta * MathUtil.easeInOutQuad(t);

        if (isExperience) {
            Hud.setCurrentHudOffset((int)result);
        }

        return result;
    }

    @Unique
    private void applyTranslation(DrawContext context, float offset) {
        context.getMatrices().push();
        context.getMatrices().translate(0, offset, 0);
    }
}
