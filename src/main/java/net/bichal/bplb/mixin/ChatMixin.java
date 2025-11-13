package net.bichal.bplb.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatHud.class)
public class ChatMixin {
//    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 2, argsOnly = true)
//    private int adjustChatY(int y) {
//        if (Hud.shouldApplyHudOffset() && Constants.CONFIG.isApplyHotbarOffset()) {
//            return y + Hud.getCurrentHudOffset();
//        }
//        return y;
//    }
}