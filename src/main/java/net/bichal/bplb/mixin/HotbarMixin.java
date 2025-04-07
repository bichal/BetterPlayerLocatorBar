package net.bichal.bplb.mixin;

import net.bichal.bplb.client.BetterPlayerLocatorBarHud;
import net.bichal.bplb.client.Keybinds;
import net.bichal.bplb.config.BetterPlayerLocatorBarConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HotbarMixin {
    @Unique
    private static final float BASE_EXPERIENCE_OFFSET = -5;
    @Unique
    private static final float TAB_OFFSET = -10;
    @Unique
    private static final float ARROW_OFFSET = -6;
    @Unique
    private static final float LERP_SPEED = 0.15f;
    @Unique
    private float experienceYOffset = 0;
    @Unique
    private float statusYOffset = 0;

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
        MinecraftClient client = MinecraftClient.getInstance();
        BetterPlayerLocatorBarConfig config = BetterPlayerLocatorBarConfig.getInstance();
        float defaultYOffset = -1;

        if (!config.isModEnabled()) {
            return MathHelper.lerp(LERP_SPEED, currentOffset, 0);
        }

        boolean hasPlayers = client.world != null && client.world.getPlayers().size() > 1;
        if (!hasPlayers || !config.isApplyHotbarOffset()) {
            return MathHelper.lerp(LERP_SPEED, currentOffset, defaultYOffset);
        }

        float targetOffset = isExperience ? BASE_EXPERIENCE_OFFSET : defaultYOffset;

        boolean shouldShowNames = config.isToggleTab() || Keybinds.shouldShowPlayerNames() || config.isAlwaysShowPlayerNames();
        boolean hasVisibleIcons = BetterPlayerLocatorBarHud.hasVisiblePlayerIcons(client);

        if (shouldShowNames && hasVisibleIcons) {
            targetOffset += TAB_OFFSET;
            if (BetterPlayerLocatorBarHud.shouldApplyGlobalArrowOffset(client)) {
                targetOffset += ARROW_OFFSET;
            }
        }

        return MathHelper.lerp(LERP_SPEED, currentOffset, targetOffset);
    }

    @Unique
    private void applyTranslation(DrawContext context, float offset) {
        context.getMatrices().push();
        context.getMatrices().translate(0, offset, 0);
    }
}
