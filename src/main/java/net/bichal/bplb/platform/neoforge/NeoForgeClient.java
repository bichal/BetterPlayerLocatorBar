package net.bichal.bplb.platform.neoforge;

import net.bichal.bplb.client.Client;
import net.bichal.bplb.client.Keybinds;
import net.bichal.bplb.command.ConfigCommand;
import net.bichal.bplb.config.ConfigScreen;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.commands.CommandSourceStack;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeClient {
    public NeoForgeClient(IEventBus modBus, ModContainer container) {
        Client.init();
        modBus.addListener(NeoForgeClient::registerKeys);
        modBus.addListener(NeoForgeClient::registerLayers);
        modBus.addListener(NeoForgeClient::registerReloadListeners);
        NeoForge.EVENT_BUS.register(GameEvents.class);
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> new ConfigScreen(parent));
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.SHOW_PLAYER_NAME);
        event.register(Keybinds.OPEN_CONFIG);
    }

    private static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "locator_bar"), (graphics, deltaTracker) -> Client.onHudRender(graphics));
    }

    private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) Client::onResourcesReloaded);
    }

    public static final class GameEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Client.onClientTick(Minecraft.getInstance());
        }

        @SubscribeEvent
        public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
            Client.onJoin(Minecraft.getInstance());
        }

        @SubscribeEvent
        public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            Client.onDisconnect();
        }

        @SubscribeEvent
        public static void onClientCommands(RegisterClientCommandsEvent event) {
            ConfigCommand.<CommandSourceStack>build(source -> new ConfigCommand.Feedback() {
                @Override
                public void success(Component message) {
                    source.sendSuccess(() -> message, false);
                }

                @Override
                public void failure(Component message) {
                    source.sendFailure(message);
                }
            }).forEach(event.getDispatcher()::register);
        }
    }
}
