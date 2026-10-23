package net.bichal.bplb.mixin;

import com.google.common.collect.Table;
import net.bichal.bplb.waypoint.ServerOps;
import net.bichal.bplb.waypoint.WaypointSync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Same connections and packets as vanilla with far fewer lookups per player movement; see {@link WaypointSync}.
 * {@code -Dbplb.optimizeWaypoints=false} leaves the vanilla code in charge.
 */
@Mixin(ServerWaypointManager.class)
public abstract class ServerWaypointManagerMixin {
    @Shadow @Final private Set<WaypointTransmitter> waypoints;
    @Shadow @Final private Set<ServerPlayer> players;
    @Shadow @Final private Table<ServerPlayer, WaypointTransmitter, WaypointTransmitter.Connection> connections;

    /** Vanilla's check (a private method there): the rule moved to per-level game rules in 1.21.11. */
    @Unique
    private static boolean bplb$ruleOn(ServerPlayer player) {
        //? if >=1.21.11 {
        /*return player.level().getGameRules().get(net.minecraft.world.level.gamerules.GameRules.LOCATOR_BAR);
        *///?} else {
        return player.level().getServer().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_LOCATOR_BAR);
        //?}
    }

    @Unique
    private ServerOps bplb$ops;

    @Unique
    private ServerOps bplb$ops() {
        if (bplb$ops == null) bplb$ops = new ServerOps(connections);
        return bplb$ops;
    }

    @Inject(method = "updateWaypoint(Lnet/minecraft/world/waypoints/WaypointTransmitter;)V", at = @At("HEAD"), cancellable = true)
    private void bplb$updateWaypoint(WaypointTransmitter transmitter, CallbackInfo ci) {
        if (!WaypointSync.enabled()) return;
        ci.cancel();
        if (!waypoints.contains(transmitter) || players.isEmpty()) return;
        // One rule per server: vanilla checks it for every pair and does nothing when it is off.
        if (!bplb$ruleOn(players.iterator().next())) return;
        WaypointSync.updateWaypoint(players, transmitter, bplb$ops());
    }

    @Inject(method = "updatePlayer", at = @At("HEAD"), cancellable = true)
    private void bplb$updatePlayer(ServerPlayer player, CallbackInfo ci) {
        if (!WaypointSync.enabled()) return;
        ci.cancel();
        if (!bplb$ruleOn(player)) return;
        WaypointSync.updatePlayer(player, waypoints, bplb$ops());
    }
}
