package net.bichal.bplb.mixin;

import net.bichal.bplb.client.Hud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class) public class ClientPlayerEntityMixin {
    @Inject(method = "respawn", at = @At("HEAD")) private void onRequestRespawn(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        Vec3 deathPos = player.position();
        Hud.addDeathLocation(deathPos);
    }
}
