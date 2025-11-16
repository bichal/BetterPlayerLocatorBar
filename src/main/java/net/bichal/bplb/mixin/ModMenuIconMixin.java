package net.bichal.bplb.mixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ModListEntry.class, ModsScreen.class})
public abstract class ModMenuIconMixin {
//    @Unique
//    private final AtlasAnimator bplb$animator = Constants.getIconAnimator();
//
//    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/terraformersmc/modmenu/gui/widget/entries/ModListEntry;getIconTexture()Lnet/minecraft/util/Identifier;"))
//    private Identifier bplb$replaceIcon(ModListEntry instance) {
//        if (!instance.getMod().getId().equals(Constants.MOD_ID)) {
//            return instance.getIconTexture();
//        }
//        int frame = bplb$animator.getCurrentFrame();
//        return new Identifier(Constants.MOD_ID, "textures/sprites/icons/frame_" + frame + ".png");
//    }
//
//    @SuppressWarnings({"MixinAnnotationTarget", "InvalidInjectorMethodSignature"}) // This works anyway 🥀
//    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lcom/terraformersmc/modmenu/gui/ModsScreen;selected:Lcom/terraformersmc/modmenu/gui/widget/entries/ModListEntry;", remap = false, opcode = Opcodes.GETFIELD), require = 0)
//    private ModListEntry bplb$replaceScreenIcon(ModsScreen instance) {
//        return instance.getSelectedEntry();
//    }
}