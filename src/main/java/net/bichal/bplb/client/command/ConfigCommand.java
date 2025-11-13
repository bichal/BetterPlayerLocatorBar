package net.bichal.bplb.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.bichal.bichalutils.util.Logger;
import net.bichal.bplb.gui.Config;
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

public class ConfigCommand {
    private static final Map<String, ConfigOption> OPTIONS = new HashMap<>();

    static {
        OPTIONS.put("mod_enabled", new BooleanOption("modEnabled", Config::isModEnabled, Config::setModEnabled));
        OPTIONS.put("hotbar_offset", new BooleanOption("applyHotbarOffset", Config::isApplyHotbarOffset, Config::setApplyHotbarOffset));
        OPTIONS.put("always_show_heads", new BooleanOption("alwaysShowPlayerHeads", Config::isAlwaysShowPlayerHeads, Config::setAlwaysShowPlayerHeads));
        OPTIONS.put("always_show_names", new BooleanOption("alwaysShowPlayerNames", Config::isAlwaysShowPlayerNames, Config::setAlwaysShowPlayerNames));
        OPTIONS.put("max_visible_icons", new IntOption(Config::getMaxVisibleIcons, Config::setMaxVisibleIcons, 1, 200));
        OPTIONS.put("lerp_speed", new FloatOption(Config::getLerpSpeed, Config::setLerpSpeed, 0.1f, 1.0f));
        OPTIONS.put("icon_size", new IntOption(Config::getIconSize, Config::setIconSize, 1, 4));
        OPTIONS.put("fade_start", new IntOption(Config::getFadeStartDistance, Config::setFadeStartDistance, 5, 9995));
        OPTIONS.put("fade_end", new IntOption(Config::getFadeEndDistance, Config::setFadeEndDistance, 10, 10000));
        OPTIONS.put("fade_alpha_max", new FloatOption(Config::getFadeAlphaMax, Config::setFadeAlphaMax, 0.01f, 1.0f));
        OPTIONS.put("fade_alpha_min", new FloatOption(Config::getFadeAlphaMin, Config::setFadeAlphaMin, 0.0f, 1.0f));
        OPTIONS.put("dot_type", new StringOption(Config::getDotType, Config::setDotType));
        OPTIONS.put("arrow_type", new StringOption(Config::getArrowType, Config::setArrowType));
        OPTIONS.put("death_marker_type", new StringOption(Config::getDeathMarkerType, Config::setDeathMarkerType));
        OPTIONS.put("nameplate_scale", new FloatOption(Config::getNameplateScale, Config::setNameplateScale, 0.5f, 1.5f));
        OPTIONS.put("vertical_padding", new IntOption(Config::getVerticalPadding, Config::setVerticalPadding, 0, 10));
        OPTIONS.put("adjust_to_fov", new BooleanOption("adjustToFov", Config::isAdjustToFov, Config::setAdjustToFov));
        OPTIONS.put("fov_multiplier", new FloatOption(Config::getFovMultiplier, Config::setFovMultiplier, 0.5f, 2.0f));
        OPTIONS.put("inherit_border_color", new BooleanOption("inheritBorderColor", Config::isInheritBorderColor, Config::setInheritBorderColor));
        OPTIONS.put("death_marker_inherit_color", new BooleanOption("deathMarkerInheritBorderColor", Config::isDeathMarkerInheritBorderColor, Config::setDeathMarkerInheritBorderColor));
    }

    public static void register() {
        Logger.info("Registering client configuration commands");
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.getRoot().addChild(createConfigCommand("betterplayerlocatorbar"));
            dispatcher.getRoot().addChild(createConfigCommand("bplb"));
        });
        Logger.info(" Client configuration commands registered");
    }


    private static LiteralCommandNode<ServerCommandSource> createConfigCommand(String rootLiteral) {
        return CommandManager.literal(rootLiteral)
                .then(CommandManager.literal("config")
                        .then(CommandManager.argument("option", StringArgumentType.word())
                                .suggests((ctx, builder) -> suggestOptions(builder))
                                .executes(ctx -> getOption(ctx.getSource(), StringArgumentType.getString(ctx, "option")))
                                .then(CommandManager.argument("value", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) -> suggestValues(builder, StringArgumentType.getString(ctx, "option")))
                                        .executes(ctx -> setOption(ctx.getSource(), StringArgumentType.getString(ctx, "option"), StringArgumentType.getString(ctx, "value")))
                                )
                        )
                ).build();
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

        Config config = Config.getInstance();
        String currentValue = option.getValue(config);
        builder.suggest(currentValue);

        if (option instanceof BooleanOption) {
            String remaining = builder.getRemaining().toLowerCase();
            if ("true".startsWith(remaining)) builder.suggest("true");
            if ("false".startsWith(remaining)) builder.suggest("false");
        }

        return builder.buildFuture();
    }

    private static int getOption(ServerCommandSource source, String optionName) {
        ConfigOption option = OPTIONS.get(optionName.toLowerCase());
        if (option == null) {
            source.sendError(Text.literal("Command not found: " + optionName).formatted(Formatting.RED));
            return 0;
        }

        Config config = Config.getInstance();
        String value = option.getValue(config);
        source.sendFeedback(() -> Text.literal("📊 " + optionName + ": ").formatted(Formatting.GOLD).append(Text.literal(value).formatted(Formatting.GREEN)), false);
        return 1;
    }

    private static int setOption(ServerCommandSource source, String optionName, String value) {
        ConfigOption option = OPTIONS.get(optionName.toLowerCase());
        if (option == null) {
            source.sendError(Text.literal("Command not found: " + optionName).formatted(Formatting.RED));
            return 0;
        }

        Config config = Config.getInstance();
        try {
            boolean success = option.setValue(config, value);
            if (success) {
                config.save();
                source.sendFeedback(() -> Text.literal(optionName + " established to: ").formatted(Formatting.GREEN).append(Text.literal(value).formatted(Formatting.GOLD)), true);
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

    interface ConfigOption {
        String getValue(Config config);

        boolean setValue(Config config, String value);
    }

    record BooleanOption(String name, Function<Config, Boolean> getter,
                         BiConsumer<Config, Boolean> setter) implements ConfigOption {

        @Override
        public String getValue(Config config) {
            return getter.apply(config) ? "true" : "false";
        }

        @Override
        public boolean setValue(Config config, String value) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                setter.accept(config, Boolean.parseBoolean(value));
                return true;
            }
            return false;
        }
    }

    record IntOption(Function<Config, Integer> getter, BiConsumer<Config, Integer> setter, int min,
                     int max) implements ConfigOption {

        @Override
        public String getValue(Config config) {
            return String.valueOf(getter.apply(config));
        }

        @Override
        public boolean setValue(Config config, String value) {
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

    record FloatOption(Function<Config, Float> getter, BiConsumer<Config, Float> setter, float min,
                       float max) implements ConfigOption {

        @Override
        public String getValue(Config config) {
            return String.format("%.2f", getter.apply(config));
        }

        @Override
        public boolean setValue(Config config, String value) {
            try {
                float floatValue = Float.parseFloat(value);
                if (floatValue >= min && floatValue <= max) {
                    setter.accept(config, floatValue);
                    return true;
                }
                return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    record StringOption(Function<Config, String> getter, BiConsumer<Config, String> setter) implements ConfigOption {

        @Override
        public String getValue(Config config) {
            return getter.apply(config);
        }

        @Override
        public boolean setValue(Config config, String value) {
            if (value != null && !value.isEmpty()) {
                setter.accept(config, value);
                return true;
            }
            return false;
        }
    }
}
