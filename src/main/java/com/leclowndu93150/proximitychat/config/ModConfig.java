package com.leclowndu93150.proximitychat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraft.ChatFormatting;
import java.util.List;

public class ModConfig {
    public static class Common {
        public final ForgeConfigSpec.IntValue proximityDistance;
        public final ForgeConfigSpec.IntValue shoutCooldown;
        public final ForgeConfigSpec.EnumValue<ChatFormatting> proximityColor;
        public final ForgeConfigSpec.EnumValue<ChatFormatting> partyColor;
        public final ForgeConfigSpec.EnumValue<ChatFormatting> shoutColor;
        public final ForgeConfigSpec.BooleanValue enableFakenameIntegration;
        public final ForgeConfigSpec.BooleanValue enableChatHeadsIntegration;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> bannedPartyNames;
        public final ForgeConfigSpec.IntValue maxPartySize;
        public final ForgeConfigSpec.BooleanValue persistPartiesOnRestart;
        public final ForgeConfigSpec.IntValue whisperDistance;
        public final ForgeConfigSpec.IntValue normalDistance;
        public final ForgeConfigSpec.IntValue yellDistance;
        public final ForgeConfigSpec.EnumValue<ChatFormatting> whisperColor;
        public final ForgeConfigSpec.EnumValue<ChatFormatting> normalColor;
        public final ForgeConfigSpec.EnumValue<ChatFormatting> yellColor;
        public final ForgeConfigSpec.IntValue yellCooldown;


        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("Chat Settings");

            proximityDistance = builder
                    .comment("Maximum distance (in blocks) for proximity chat")
                    .defineInRange("proximityDistance", 100, 10, 1000);

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

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }
}
