package net.bichal.bplb.mixin;

import net.bichal.bichalutils.util.MathUtil;
import net.bichal.bplb.client.Hud;
import net.bichal.bplb.client.Keybinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.bichal.bplb.util.Constants.CONFIG;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Unique private static float chatYOffset = 0;
    @Unique private static long lastPlayerVisibleTime = 0;
    @Unique private static final int MAX_OFFSET_THRESHOLD = 40;

    @Inject(method = "render", at = @At("HEAD"))
    private void adjustChatRender(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (shouldFreeze() || client.currentScreen != null) return;
        
        chatYOffset = updateChatOffset(chatYOffset);
        context.getMatrices().push();
        context.getMatrices().translate(0, chatYOffset, 0);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void resetChatRender(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (shouldFreeze() || client.currentScreen != null) return;
        context.getMatrices().pop();
    }

    @Unique
    private static boolean shouldFreeze() {
        return CONFIG.getGlobalHudYOffset() < -MAX_OFFSET_THRESHOLD;
    }

    @Unique
    private static float updateChatOffset(float currentOffset) {
        float t = Math.min(CONFIG.getLerpSpeed() * 0.5f, 1.0f);
        int globalOffset = CONFIG.getGlobalHudYOffset();

        if (!CONFIG.isModEnabled()) {
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

        if (!CONFIG.isApplyHotbarOffset() && !showingTab) {
            float targetOffset = globalOffset - 1;
            float delta = targetOffset - currentOffset;
            return currentOffset + delta * MathUtil.easeInOutQuad(t);
        }

        int baseOffset = globalOffset + (-1);
        
        if (showingTab && baseOffset <= 0) {
            baseOffset -= (18 + (int) (12 * CONFIG.getNameplateScale()));
        }

        float delta = baseOffset - currentOffset;
        return currentOffset + delta * MathUtil.easeInOutQuad(t);
    }
}