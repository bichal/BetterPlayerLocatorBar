package net.bichal.bplb.mixin;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModListWidget.class) public class ModMenuIconMixin {
//    @Inject(method = "renderList", at = @At("HEAD"))
//    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//        if (ModConfig.animateModMenuIcon && Constants.getIconAnimator() != null) {
//            Constants.getIconAnimator().getCurrentFrame();
//        }
//    }
}