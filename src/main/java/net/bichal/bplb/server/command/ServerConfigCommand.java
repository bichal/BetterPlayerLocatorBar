package net.bichal.bplb.server.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.bichal.bichalutils.util.Logger;
import net.bichal.bplb.server.ServerConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ServerConfigCommand {
    private static final Map<String, ConfigOption> OPTIONS = new HashMap<>();

    static {
        OPTIONS.put("update_rate", new IntOption(ServerConfig::positionUpdateRateTicks, ServerConfig::setPositionUpdateRateTicks, 1, 100));
        OPTIONS.put("position_threshold", new DoubleOption(ServerConfig::positionChangeThreshold, ServerConfig::setPositionChangeThreshold, 0.01, 10.0));
        OPTIONS.put("max_distance", new DoubleOption(ServerConfig::maxRelevantDistance, ServerConfig::setMaxRelevantDistance, 64.0, 2048.0));
        OPTIONS.put("thread_pool_size", new IntOption(ServerConfig::threadPoolSize, ServerConfig::setThreadPoolSize, 1, Runtime.getRuntime().availableProcessors()));
        OPTIONS.put("use_datapack_fallback", new BooleanOption("useDatapackFallback", ServerConfig::useDatapackFallback, ServerConfig::setUseDatapackFallback));
        OPTIONS.put("preset", new PresetOption());
    }

    public static void register() {
        Logger.info("Registering server configuration commands");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.getRoot().addChild(createConfigCommand("betterplayerlocatorbar"));
            dispatcher.getRoot().addChild(createConfigCommand("bplb"));
        });

        Logger.info("Server configuration commands registered");
    }

    private static LiteralCommandNode<ServerCommandSource> createConfigCommand(String rootLiteral) {
        return CommandManager.literal(rootLiteral).requires(source -> source.hasPermissionLevel(2)).then(CommandManager.literal("config").then(CommandManager.argument("option", StringArgumentType.word()).suggests((ctx, builder) -> suggestOptions(builder)).executes(ctx -> getOption(ctx.getSource(), StringArgumentType.getString(ctx, "option"))).then(CommandManager.argument("value", StringArgumentType.greedyString()).suggests((ctx, builder) -> suggestValues(builder, StringArgumentType.getString(ctx, "option"))).executes(ctx -> setOption(ctx.getSource(), StringArgumentType.getString(ctx, "option"), StringArgumentType.getString(ctx, "value"))))).then(CommandManager.literal("info").executes(ctx -> showInfo(ctx.getSource())))).build();
    }

    private static CompletableFuture<Suggestions> suggestOptions(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        for (String option : OPTIONS.keySet()) {
            if (option.startsWith(remaining)) {
                builder.suggest(option);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestValues(SuggestionsBuilder builder, String optionName) {
        ConfigOption option = OPTIONS.get(optionName.toLowerCase());
        if (option == null) return builder.buildFuture();

        ServerConfig config = ServerConfig.getInstance();
        String currentValue = option.getValue(config);
        builder.suggest(currentValue);

        if (option instanceof BooleanOption) {
            String remaining = builder.getRemaining().toLowerCase();
            if ("true".startsWith(remaining)) builder.suggest("true");
            if ("false".startsWith(remaining)) builder.suggest("false");
        } else if (option instanceof PresetOption) {
            builder.suggest("minimal");
            builder.suggest("recommended");
            builder.suggest("insane");
        }

        return builder.buildFuture();
    }

    private static int getOption(ServerCommandSource source, String optionName) {
        ConfigOption option = OPTIONS.get(optionName.toLowerCase());
        if (option == null) {
            source.sendError(Text.literal("Unknown option: " + optionName).formatted(Formatting.RED));
            return 0;
        }

        ServerConfig config = ServerConfig.getInstance();
        String value = option.getValue(config);

        source.sendFeedback(() -> Text.literal("📊 " + optionName + ": ").formatted(Formatting.GOLD).append(Text.literal(value).formatted(Formatting.GREEN)), false);
        return 1;
    }

    private static int setOption(ServerCommandSource source, String optionName, String value) {
        ConfigOption option = OPTIONS.get(optionName.toLowerCase());
        if (option == null) {
            source.sendError(Text.literal("Unknown option: " + optionName).formatted(Formatting.RED));
            return 0;
        }

        ServerConfig config = ServerConfig.getInstance();
        try {
            boolean success = option.setValue(config, value);
            if (success) {
                config.save();
                source.sendFeedback(() -> Text.literal(optionName + " set to: ").formatted(Formatting.GREEN).append(Text.literal(value).formatted(Formatting.GOLD)), true);
                return 1;
            } else {
                source.sendError(Text.literal("Invalid value: " + value).formatted(Formatting.RED));
                return 0;
            }
        } catch (Exception e) {
            source.sendError(Text.literal("Error: " + e.getMessage()).formatted(Formatting.RED));
            return 0;
        }
    }

    private static int showInfo(ServerCommandSource source) {
        ServerConfig config = ServerConfig.getInstance();

        source.sendFeedback(() -> Text.literal("=== BPLB Server Config ===").formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.literal("Preset: " + config.preset()).formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("Update Rate: " + config.positionUpdateRateTicks() + " ticks"), false);
        source.sendFeedback(() -> Text.literal("Position Threshold: " + config.positionChangeThreshold() + " blocks"), false);
        source.sendFeedback(() -> Text.literal("Max Distance: " + config.maxRelevantDistance() + " blocks"), false);
        source.sendFeedback(() -> Text.literal("Thread Pool Size: " + config.threadPoolSize()), false);
        source.sendFeedback(() -> Text.literal("Datapack Fallback: " + config.useDatapackFallback()), false);

        return 1;
    }

    // Config option interfaces
    interface ConfigOption {
        String getValue(ServerConfig config);

        boolean setValue(ServerConfig config, String value);
    }

    record BooleanOption(String name, Function<ServerConfig, Boolean> getter,
                         BiConsumer<ServerConfig, Boolean> setter) implements ConfigOption {
        @Override
        public String getValue(ServerConfig config) {
            return getter.apply(config) ? "true" : "false";
        }

        @Override
        public boolean setValue(ServerConfig config, String value) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                setter.accept(config, Boolean.parseBoolean(value));
                return true;
            }
            return false;
        }
    }

    record IntOption(Function<ServerConfig, Integer> getter, BiConsumer<ServerConfig, Integer> setter, int min,
                     int max) implements ConfigOption {
        @Override
        public String getValue(ServerConfig config) {
            return String.valueOf(getter.apply(config));
        }

        @Override
        public boolean setValue(ServerConfig config, String value) {
            try {
                int intValue = Integer.parseInt(value);
                if (intValue >= min && intValue <= max) {
                    setter.accept(config, intValue);
                    return true;
                }
                return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    record DoubleOption(Function<ServerConfig, Double> getter, BiConsumer<ServerConfig, Double> setter, double min,
                        double max) implements ConfigOption {
        @Override
        public String getValue(ServerConfig config) {
            return String.format("%.2f", getter.apply(config));
        }

        @Override
        public boolean setValue(ServerConfig config, String value) {
            try {
                double doubleValue = Double.parseDouble(value);
                if (doubleValue >= min && doubleValue <= max) {
                    setter.accept(config, doubleValue);
                    return true;
                }
                return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    static class PresetOption implements ConfigOption {
        @Override
        public String getValue(ServerConfig config) {
            return config.preset();
        }

        @Override
        public boolean setValue(ServerConfig config, String value) {
            String preset = value.toLowerCase();
            if (preset.equals("minimal") || preset.equals("recommended") || preset.equals("insane")) {
                config.applyPreset(preset);
                return true;
            }
            return false;
        }
    }
}
