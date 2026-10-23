package net.bichal.bplb.platform.fabric;

import net.bichal.bplb.client.Client;
import net.bichal.bplb.client.Keybinds;
import net.bichal.bplb.command.ConfigCommand;
//? if !waypoints {
import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.network.PositionUpdatePayload;
//?}
import net.bichal.bplb.util.Constants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
//? if !waypoints {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
//?}
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Client.init();

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "asset_scanner");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                Client.onResourcesReloaded(manager);
            }
        });

        // From 1.21.6 the bar is drawn from the vanilla locator bar renderer (see LocatorBarMixin).
        //? if !waypoints {
        HudRenderCallback.EVENT.register((context, tickCounter) -> Client.onHudRender(context));
        //?}
        ClientTickEvents.END_CLIENT_TICK.register(Client::onClientTick);

        KeyBindingHelper.registerKeyBinding(Keybinds.SHOW_PLAYER_NAME);
        KeyBindingHelper.registerKeyBinding(Keybinds.OPEN_CONFIG);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> Client.onJoin(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> Client.onDisconnect());

        //? if !waypoints {
        ClientPlayNetworking.registerGlobalReceiver(HandshakePayload.TYPE, (payload, context) -> Client.onHandshake(payload));
        ClientPlayNetworking.registerGlobalReceiver(PositionUpdatePayload.TYPE, (payload, context) -> Client.onPositionUpdate(payload));
        //?}

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                ConfigCommand.<FabricClientCommandSource>build(source -> new ConfigCommand.Feedback() {
                    @Override
                    public void success(Component message) {
                        source.sendFeedback(message);
                    }

                    @Override
                    public void failure(Component message) {
                        source.sendError(message);
                    }
                }).forEach(dispatcher::register));
    }
}
