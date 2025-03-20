package net.bichal.bplb.mixin;

import net.bichal.bplb.client.BetterPlayerLocatorBarHud;
import net.bichal.bplb.client.Keybinds;
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
    private static final float BASE_EXPERIENCE_OFFSET = -3;
    @Unique
    private static final float TAB_OFFSET = -9;
    @Unique
    private static final float ARROW_OFFSET = -5;
    @Unique
    private static final float LERP_SPEED = 0.15f;
    @Unique
    private float experienceYOffset = 0;
    @Unique
    private float statusYOffset = 0;

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"))
    private void adjustExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        experienceYOffset = updateYOffset(BASE_EXPERIENCE_OFFSET, experienceYOffset);
        applyTranslation(context, experienceYOffset);
    }

    @Inject(method = "renderExperienceLevel", at = @At("RETURN"))
    private void resetExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void adjustStatusBars(DrawContext context, CallbackInfo ci) {
        statusYOffset = updateYOffset(0, statusYOffset);
        applyTranslation(context, statusYOffset);
    }

    @Inject(method = "renderStatusBars", at = @At("RETURN"))
    private void resetStatusBars(DrawContext context, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Unique
    private float updateYOffset(float baseOffset, float currentOffset) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean hasPlayers = client.world != null && client.world.getPlayers().size() > 1;
        boolean isTabPressed = Keybinds.SHOW_PLAYER_NAME.isPressed() && hasPlayers;

        float targetOffset = hasPlayers ? baseOffset : 0;
        if (isTabPressed) targetOffset += TAB_OFFSET;
        if (BetterPlayerLocatorBarHud.shouldApplyArrowOffset(client)) targetOffset += ARROW_OFFSET;

        return MathHelper.lerp(LERP_SPEED, currentOffset, targetOffset);
    }

    @Unique
    private void applyTranslation(DrawContext context, float offset) {
        context.getMatrices().push();
        context.getMatrices().translate(0, offset, 0);
    }
}
