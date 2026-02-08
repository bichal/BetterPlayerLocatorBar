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

/**
 * Mixin for InGameHud to adjust hotbar position when player names are visible.
 * This provides smooth transitions for the HUD offset based on player visibility.
 * Updated for Minecraft 1.21.6 API.
 */
@Mixin(InGameHud.class)
public final class HotbarMixin {
    /** Base Y offset for experience bar when visible */
    @Unique
    private static final float BASE_EXPERIENCE_OFFSET = -5;
    
    /** Tab list offset based on nameplate scale */
    @Unique
    private static final int TAB_OFFSET = (int) -(18 * CONFIG.getNameplateScale());
    
    /** Current smoothed experience bar Y offset */
    @Unique
    private float bplb_experienceYOffset = 0;
    
    /** Current smoothed status bar Y offset */
    @Unique
    private float bplb_statusYOffset = 0;
    
    /** Timestamp of last player visibility for smooth hide animation */
    @Unique
    private static long bplb_lastPlayerVisibleTime = 0;

    /**
     * Adjusts experience bar position during main HUD rendering.
     * 1.21.6 draws experience level inside renderMainHud via Bar.drawExperienceLevel.
     * We push/pop around that invocation to avoid offsetting unrelated HUD elements.
     */
    @Inject(
            method = "renderMainHud",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/bar/Bar;drawExperienceLevel(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;I)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void bplb$beforeExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        bplb_experienceYOffset = bplb$updateYOffset(true, bplb_experienceYOffset);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, bplb_experienceYOffset + 1);
    }

    @Inject(
            method = "renderMainHud",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/bar/Bar;drawExperienceLevel(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;I)V",
                    shift = At.Shift.AFTER
            )
    )
    private void bplb$afterExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }

    /**
     * Adjusts status bars position at the start of rendering.
     */
    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void bplb$adjustStatusBars(DrawContext context, CallbackInfo ci) {
        bplb_statusYOffset = bplb$updateYOffset(false, bplb_statusYOffset);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, bplb_statusYOffset);
    }

    @Inject(method = "renderStatusBars", at = @At("RETURN"))
    private void bplb$resetStatusBars(DrawContext context, CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }

    /**
     * Calculates and updates the Y offset for HUD elements.
     * Uses smooth interpolation for transitions.
     * 
     * @param isExperience Whether calculating for experience bar
     * @param currentOffset Current offset value
     * @return New smoothed offset value
     */
    @Unique
    private float bplb$updateYOffset(boolean isExperience, float currentOffset) {
        float t = Math.min(CONFIG.getLerpSpeed() * 0.5f, 1.0f);
        float defaultYOffset = -1;

        // Mod disabled - return to zero
        if (!CONFIG.isModEnabled()) {
            return bplb$interpolate(currentOffset, 0, t);
        }

        boolean shouldOffset = Hud.shouldApplyHudOffset();
        long currentTime = System.currentTimeMillis();

        // Track when player was last visible
        if (shouldOffset) {
            bplb_lastPlayerVisibleTime = currentTime;
        }

        boolean recentlyVisible = (currentTime - bplb_lastPlayerVisibleTime) < 3000;

        // Player should not be offset - return to default or zero
        if (!shouldOffset) {
            float targetOffset;
            if (recentlyVisible) {
                targetOffset = isExperience ? BASE_EXPERIENCE_OFFSET : defaultYOffset;
            } else {
                targetOffset = 0;
            }
            return bplb$interpolate(currentOffset, targetOffset, t);
        }

        // Hotbar offset disabled - return to base offset
        if (!CONFIG.isApplyHotbarOffset()) {
            float targetOffset = isExperience ? BASE_EXPERIENCE_OFFSET : defaultYOffset;
            return bplb$interpolate(currentOffset, targetOffset, t);
        }

        // Calculate full offset with tab list adjustment
        int targetOffset = (int) (isExperience ? BASE_EXPERIENCE_OFFSET : defaultYOffset);
        boolean shouldShowNames = Keybinds.shouldShowPlayerNames() || CONFIG.isAlwaysShowPlayerNames();
        if (shouldShowNames) {
            targetOffset += TAB_OFFSET;
        }

        return bplb$interpolate(currentOffset, targetOffset, t);
    }

    /**
     * Linearly interpolates between current and target values with easing.
     */
    @Unique
    private float bplb$interpolate(float current, float target, float t) {
        float delta = target - current;
        return current + delta * MathUtils.easeInOutQuad(t);
    }
}
