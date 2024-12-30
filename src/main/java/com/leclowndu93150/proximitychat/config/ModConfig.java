package com.leclowndu93150.proximitychat.config;

import com.leclowndu93150.proximitychat.ProximityChatMod;
import net.minecraft.ChatFormatting;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

@EventBusSubscriber(modid = ProximityChatMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModConfig {
    private static int proximityDistance;
    private static int shoutCooldown;
    private static ChatFormatting proximityColor;
    private static ChatFormatting partyColor;
    private static ChatFormatting shoutColor;
    private static boolean enableFakenameIntegration;
    private static boolean enableChatHeadsIntegration;
    private static List<? extends String> bannedPartyNames;
    private static int maxPartySize;
    private static boolean persistPartiesOnRestart;
    private static int whisperDistance;
    private static int normalDistance;
    private static int yellDistance;
    private static ChatFormatting whisperColor;
    private static ChatFormatting normalColor;
    private static ChatFormatting yellColor;
    private static int yellCooldown;

    public static class Common {
        public final ModConfigSpec.IntValue proximityDistance;
        public final ModConfigSpec.IntValue shoutCooldown;
        public final ModConfigSpec.EnumValue<ChatFormatting> proximityColor;
        public final ModConfigSpec.EnumValue<ChatFormatting> partyColor;
        public final ModConfigSpec.EnumValue<ChatFormatting> shoutColor;
        public final ModConfigSpec.BooleanValue enableFakenameIntegration;
        public final ModConfigSpec.BooleanValue enableChatHeadsIntegration;
        public final ModConfigSpec.ConfigValue<List<? extends String>> bannedPartyNames;
        public final ModConfigSpec.IntValue maxPartySize;
        public final ModConfigSpec.BooleanValue persistPartiesOnRestart;
        public final ModConfigSpec.IntValue whisperDistance;
        public final ModConfigSpec.IntValue normalDistance;
        public final ModConfigSpec.IntValue yellDistance;
        public final ModConfigSpec.EnumValue<ChatFormatting> whisperColor;
        public final ModConfigSpec.EnumValue<ChatFormatting> normalColor;
        public final ModConfigSpec.EnumValue<ChatFormatting> yellColor;
        public final ModConfigSpec.IntValue yellCooldown;

        public Common(ModConfigSpec.Builder builder) {
            builder.push("Chat Settings");

            proximityDistance = builder
                    .comment("Maximum distance (in blocks) for proximity chat")
                    .defineInRange("proximityDistance", 50, 10, 1000);

            shoutCooldown = builder
                    .comment("Cooldown (in seconds) between shouts")
                    .defineInRange("shoutCooldown", 30, 0, 3600);

            proximityColor = builder
                    .comment("Color for proximity chat messages")
                    .defineEnum("proximityColor", ChatFormatting.WHITE);

            partyColor = builder
                    .comment("Color for party chat messages")
                    .defineEnum("partyColor", ChatFormatting.GREEN);

            shoutColor = builder
                    .comment("Color for shout messages")
                    .defineEnum("shoutColor", ChatFormatting.RED);

            builder.pop();

            builder.push("Party Settings");

            maxPartySize = builder
                    .comment("Maximum number of players allowed in a party")
                    .defineInRange("maxPartySize", 10, 2, 100);

            bannedPartyNames = builder
                    .comment("List of banned party names")
                    .defineList("bannedPartyNames",
                            List.of("admin", "mod", "staff"),
                            entry -> entry instanceof String);

            persistPartiesOnRestart = builder
                    .comment("Whether parties should persist after server restart")
                    .define("persistPartiesOnRestart", true);

            builder.pop();

            builder.push("Integration Settings");

            enableFakenameIntegration = builder
                    .comment("Enable integration with Fakename mod")
                    .define("enableFakenameIntegration", true);

            enableChatHeadsIntegration = builder
                    .comment("Enable integration with Chat Heads mod")
                    .define("enableChatHeadsIntegration", true);

            builder.pop();

            builder.push("Chat Range Settings");

            whisperDistance = builder
                    .comment("Maximum distance (in blocks) for whisper chat")
                    .defineInRange("whisperDistance", 15, 5, 30);

            normalDistance = builder
                    .comment("Maximum distance (in blocks) for normal chat")
                    .defineInRange("normalDistance", 50, 15, 100);

            yellDistance = builder
                    .comment("Maximum distance (in blocks) for yelling")
                    .defineInRange("yellDistance", 100, 50, 200);

            whisperColor = builder
                    .comment("Color for whisper messages")
                    .defineEnum("whisperColor", ChatFormatting.GRAY);

            normalColor = builder
                    .comment("Color for normal chat messages")
                    .defineEnum("normalColor", ChatFormatting.WHITE);

            yellColor = builder
                    .comment("Color for yell messages")
                    .defineEnum("yellColor", ChatFormatting.YELLOW);

            yellCooldown = builder
                    .comment("Cooldown (in seconds) between yells")
                    .defineInRange("yellCooldown", 30, 0, 3600);

            builder.pop();
        }
    }

    public static final ModConfigSpec SERVER_SPEC;
    public static final Common COMMON;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        SERVER_SPEC = builder.build();
    }

    private static void refreshConfig() {
        proximityDistance = COMMON.proximityDistance.get();
        shoutCooldown = COMMON.shoutCooldown.get();
        proximityColor = COMMON.proximityColor.get();
        partyColor = COMMON.partyColor.get();
        shoutColor = COMMON.shoutColor.get();
        enableFakenameIntegration = COMMON.enableFakenameIntegration.get();
        enableChatHeadsIntegration = COMMON.enableChatHeadsIntegration.get();
        bannedPartyNames = COMMON.bannedPartyNames.get();
        maxPartySize = COMMON.maxPartySize.get();
        persistPartiesOnRestart = COMMON.persistPartiesOnRestart.get();
        whisperDistance = COMMON.whisperDistance.get();
        normalDistance = COMMON.normalDistance.get();
        yellDistance = COMMON.yellDistance.get();
        whisperColor = COMMON.whisperColor.get();
        normalColor = COMMON.normalColor.get();
        yellColor = COMMON.yellColor.get();
        yellCooldown = COMMON.yellCooldown.get();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        if (configEvent.getConfig().getType() == net.neoforged.fml.config.ModConfig.Type.SERVER) {
            refreshConfig();
        }
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) {
        if (configEvent.getConfig().getType() == net.neoforged.fml.config.ModConfig.Type.SERVER) {
            refreshConfig();
        }
    }

}