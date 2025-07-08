package net.bichal.bplb.mixin;

import net.bichal.bplb.client.Hud;
import net.bichal.bplb.client.Keybinds;
import net.bichal.bplb.config.Config;
import net.bichal.bplb.util.Constants;
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
    private float experienceYOffset = 0f;
    @Unique
    private float statusYOffset = 0f;

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"))
    private void adjustExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        experienceYOffset = updateYOffset(true, experienceYOffset);
        applyTranslation(context, experienceYOffset);
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
        final MinecraftClient client = MinecraftClient.getInstance();
        final Config config = Config.getInstance();

        if (!config.isModEnabled() || !config.isApplyHotbarOffset() || client.player == null || !Hud.hasVisiblePlayerIcons()) {
            return MathHelper.lerp(Constants.HOTBAR_LERP_SPEED, currentOffset, 0);
        }

        float targetOffset = 0;
        final boolean isBarActive = config.isToggleTab() || Keybinds.shouldShowPlayerNames() || config.isAlwaysShowPlayerNames();

        if (isBarActive) {
            float nameplateHeight = (client.textRenderer.fontHeight * config.getNameplateScale()) + 10;
            targetOffset -= (Constants.ICON_BASE_SIZE / 2.0f + nameplateHeight + config.getVerticalPadding());
            if (Hud.shouldApplyGlobalArrowOffset(client)) {
                targetOffset += Constants.HOTBAR_ARROW_OFFSET;
            }
        }

        if (isExperience) {
            targetOffset += Constants.HOTBAR_BASE_EXPERIENCE_OFFSET;
        }

        return MathHelper.lerp(Constants.HOTBAR_LERP_SPEED, currentOffset, targetOffset);
    }

    @Unique
    private void applyTranslation(DrawContext context, float offset) {
        context.getMatrices().push();
        context.getMatrices().translate(0, offset, 0);
    }
}
