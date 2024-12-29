package com.leclowndu93150.proximitychat.config;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraft.ChatFormatting;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("proximitychat", "config"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static class ConfigSyncPacket {
        private final int proximityDistance;
        private final int shoutCooldown;
        private final ChatFormatting proximityColor;
        private final ChatFormatting partyColor;
        private final ChatFormatting shoutColor;
        private final boolean enableFakenameIntegration;
        private final boolean enableChatHeadsIntegration;
        private final List<? extends String> bannedPartyNames;
        private final int maxPartySize;
        private final boolean persistPartiesOnRestart;
        private final int whisperDistance;
        private final int normalDistance;
        private final int yellDistance;
        private final ChatFormatting whisperColor;
        private final ChatFormatting normalColor;
        private final ChatFormatting yellColor;
        private final int yellCooldown;

        public ConfigSyncPacket(Common config) {
            this.proximityDistance = config.proximityDistance.get();
            this.shoutCooldown = config.shoutCooldown.get();
            this.proximityColor = config.proximityColor.get();
            this.partyColor = config.partyColor.get();
            this.shoutColor = config.shoutColor.get();
            this.enableFakenameIntegration = config.enableFakenameIntegration.get();
            this.enableChatHeadsIntegration = config.enableChatHeadsIntegration.get();
            this.bannedPartyNames = config.bannedPartyNames.get();
            this.maxPartySize = config.maxPartySize.get();
            this.persistPartiesOnRestart = config.persistPartiesOnRestart.get();
            this.whisperDistance = config.whisperDistance.get();
            this.normalDistance = config.normalDistance.get();
            this.yellDistance = config.yellDistance.get();
            this.whisperColor = config.whisperColor.get();
            this.normalColor = config.normalColor.get();
            this.yellColor = config.yellColor.get();
            this.yellCooldown = config.yellCooldown.get();
        }

        private ConfigSyncPacket(int proximityDistance, int shoutCooldown, ChatFormatting proximityColor,
                                 ChatFormatting partyColor, ChatFormatting shoutColor, boolean enableFakenameIntegration,
                                 boolean enableChatHeadsIntegration, List<? extends String> bannedPartyNames,
                                 int maxPartySize, boolean persistPartiesOnRestart, int whisperDistance,
                                 int normalDistance, int yellDistance, ChatFormatting whisperColor,
                                 ChatFormatting normalColor, ChatFormatting yellColor, int yellCooldown) {
            this.proximityDistance = proximityDistance;
            this.shoutCooldown = shoutCooldown;
            this.proximityColor = proximityColor;
            this.partyColor = partyColor;
            this.shoutColor = shoutColor;
            this.enableFakenameIntegration = enableFakenameIntegration;
            this.enableChatHeadsIntegration = enableChatHeadsIntegration;
            this.bannedPartyNames = bannedPartyNames;
            this.maxPartySize = maxPartySize;
            this.persistPartiesOnRestart = persistPartiesOnRestart;
            this.whisperDistance = whisperDistance;
            this.normalDistance = normalDistance;
            this.yellDistance = yellDistance;
            this.whisperColor = whisperColor;
            this.normalColor = normalColor;
            this.yellColor = yellColor;
            this.yellCooldown = yellCooldown;
        }

        public static void encode(ConfigSyncPacket packet, FriendlyByteBuf buffer) {
            buffer.writeInt(packet.proximityDistance);
            buffer.writeInt(packet.shoutCooldown);
            buffer.writeEnum(packet.proximityColor);
            buffer.writeEnum(packet.partyColor);
            buffer.writeEnum(packet.shoutColor);
            buffer.writeBoolean(packet.enableFakenameIntegration);
            buffer.writeBoolean(packet.enableChatHeadsIntegration);
            buffer.writeCollection(packet.bannedPartyNames, FriendlyByteBuf::writeUtf);
            buffer.writeInt(packet.maxPartySize);
            buffer.writeBoolean(packet.persistPartiesOnRestart);
            buffer.writeInt(packet.whisperDistance);
            buffer.writeInt(packet.normalDistance);
            buffer.writeInt(packet.yellDistance);
            buffer.writeEnum(packet.whisperColor);
            buffer.writeEnum(packet.normalColor);
            buffer.writeEnum(packet.yellColor);
            buffer.writeInt(packet.yellCooldown);
        }

        public static ConfigSyncPacket decode(FriendlyByteBuf buffer) {
            return new ConfigSyncPacket(
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readEnum(ChatFormatting.class),
                    buffer.readEnum(ChatFormatting.class),
                    buffer.readEnum(ChatFormatting.class),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readList(FriendlyByteBuf::readUtf),
                    buffer.readInt(),
                    buffer.readBoolean(),
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readEnum(ChatFormatting.class),
                    buffer.readEnum(ChatFormatting.class),
                    buffer.readEnum(ChatFormatting.class),
                    buffer.readInt()
            );
        }

        public static void handle(ConfigSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                COMMON.proximityDistance.set(packet.proximityDistance);
                COMMON.shoutCooldown.set(packet.shoutCooldown);
                COMMON.proximityColor.set(packet.proximityColor);
                COMMON.partyColor.set(packet.partyColor);
                COMMON.shoutColor.set(packet.shoutColor);
                COMMON.enableFakenameIntegration.set(packet.enableFakenameIntegration);
                COMMON.enableChatHeadsIntegration.set(packet.enableChatHeadsIntegration);
                COMMON.bannedPartyNames.set(packet.bannedPartyNames);
                COMMON.maxPartySize.set(packet.maxPartySize);
                COMMON.persistPartiesOnRestart.set(packet.persistPartiesOnRestart);
                COMMON.whisperDistance.set(packet.whisperDistance);
                COMMON.normalDistance.set(packet.normalDistance);
                COMMON.yellDistance.set(packet.yellDistance);
                COMMON.whisperColor.set(packet.whisperColor);
                COMMON.normalColor.set(packet.normalColor);
                COMMON.yellColor.set(packet.yellColor);
                COMMON.yellCooldown.set(packet.yellCooldown);
            });
            ctx.get().setPacketHandled(true);
        }
    }

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
                    .defineInRange("proximityDistance", 50, 10, 1000);

            shoutCooldown = builder
                    .comment("Cooldown (in seconds) between shouts")
                    .defineInRange("shoutCooldown", 0, 0, 3600);

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
                    .defineInRange("yellCooldown", 0, 0, 3600);

            builder.pop();
        }
    }

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();

        // Register the sync packet
        NETWORK.registerMessage(0, ConfigSyncPacket.class,
                ConfigSyncPacket::encode,
                ConfigSyncPacket::decode,
                ConfigSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            syncConfig();
        }
    }

    @Mod.EventBusSubscriber(modid = "proximitychat", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity().level.isClientSide()) return;

            NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)event.getEntity()),
                    new ConfigSyncPacket(COMMON));
        }
    }

    private static void syncConfig() {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            NETWORK.send(PacketDistributor.ALL.noArg(), new ConfigSyncPacket(COMMON));
        }
    }
}