package net.bichal.bplb.mixin;

import net.bichal.bplb.client.Hud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class) public class ClientPlayerEntityMixin {
    @Inject(method = "requestRespawn", at = @At("HEAD")) private void onRequestRespawn(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        Vec3d deathPos = player.getPos();
        Hud.addDeathLocation(deathPos);
    }
}
