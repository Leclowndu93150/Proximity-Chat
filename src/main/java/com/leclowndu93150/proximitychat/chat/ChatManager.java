package com.leclowndu93150.proximitychat.chat;

import com.leclowndu93150.proximitychat.config.ModConfig;
import com.leclowndu93150.proximitychat.data.DataManager;
import com.leclowndu93150.proximitychat.data.PartyData;
import com.leclowndu93150.proximitychat.data.PlayerData;
import com.leclowndu93150.proximitychat.party.PartyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;

public class ChatManager {
    public enum ChatRange {
        WHISPER,
        NORMAL,
        YELL
    }

    public static void sendRangedMessage(ServerPlayer sender, String message, ChatRange range) {
        int maxDistance;
        ChatFormatting color;
        Component prefix;

        switch (range) {
            case WHISPER:
                maxDistance = ModConfig.COMMON.whisperDistance.get();
                color = ModConfig.COMMON.whisperColor.get();
                prefix = Component.literal("whispers ").withStyle(style -> style.withItalic(true).withColor(ChatFormatting.GRAY));
                break;
            case YELL:
                maxDistance = ModConfig.COMMON.yellDistance.get();
                color = ModConfig.COMMON.yellColor.get();
                prefix = Component.literal("yells ").withStyle(style -> style.withItalic(true).withColor(ChatFormatting.GRAY));
                break;
            default:
                maxDistance = ModConfig.COMMON.normalDistance.get();
                color = ModConfig.COMMON.normalColor.get();
                prefix = Component.empty();
                break;
        }

        for (ServerPlayer player : sender.getServer().getPlayerList().getPlayers()) {
            double distance = sender.distanceTo(player);
            if (distance <= maxDistance) {
                Component distanceText = Component.literal(String.format("(%s away) ", formatDistance(distance)))
                        .withStyle(ChatFormatting.GRAY);

                Component nameText = Component.literal(String.format("[%s] ", getDisplayName(sender)))  // Added space here
                        .withStyle(color);

                Component messageText = Component.literal(message)
                        .withStyle(color);

                Component finalMessage = prefix.copy()
                        .append(distanceText)
                        .append(nameText)
                        .append(messageText);

                if (distance > maxDistance * 0.8) {
                    finalMessage = finalMessage.copy().withStyle(ChatFormatting.DARK_GRAY);
                }

                player.sendSystemMessage(finalMessage);
            }
        }
    }

    public static void sendProximityMessage(ServerPlayer sender, String message) {
        double maxDistance = ModConfig.COMMON.proximityDistance.get();
        ChatFormatting color = ModConfig.COMMON.proximityColor.get();

        for (ServerPlayer player : sender.getServer().getPlayerList().getPlayers()) {
            double distance = sender.distanceTo(player);
            if (distance <= maxDistance) {
                Component finalMessage = Component.empty()
                        .append(Component.literal(String.format("(%dm away) ", (int)distance))
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("[")
                                .withStyle(color))
                        .append(Component.literal(getDisplayName(sender) + "] ")  // Added space here
                                .withStyle(color))
                        .append(Component.literal(message)
                                .withStyle(color));

                player.sendSystemMessage(finalMessage);
            }
        }
    }

    public static void sendPartyMessage(ServerPlayer sender, PartyData party, String message) {
        ChatFormatting color = ModConfig.COMMON.partyColor.get();

        for (ServerPlayer player : sender.getServer().getPlayerList().getPlayers()) {
            if (party.getMembers().contains(player.getUUID())) {
                Component chatMessage = Component.literal("")
                        .withStyle(color)
                        .append(Component.literal(String.format("(%s) ", party.getName())))
                        .append(Component.literal(String.format("(%dm away) ", (int) sender.distanceTo(player))))
                        .append(Component.literal(String.format("[%s] ", getDisplayName(sender))))  // Added space here
                        .append(Component.literal(message));

                player.sendSystemMessage(chatMessage);
            }
        }
    }

    public static void sendShoutMessage(ServerPlayer sender, String message) {
        PlayerData data = PartyManager.getOrCreatePlayerData(sender.getUUID());
        long currentTime = System.currentTimeMillis();
        long lastShout = data.getLastShoutTime();
        int cooldown = ModConfig.COMMON.shoutCooldown.get() * 1000;

        if (currentTime - lastShout >= cooldown) {
            ChatFormatting color = ModConfig.COMMON.shoutColor.get();

            Component shoutMessage = Component.literal(
                    String.format("( !! SHOUT !! ) [%s]: %s",
                            getDisplayName(sender),
                            message
                    )
            ).withStyle(color);

            for (ServerPlayer player : sender.getServer().getPlayerList().getPlayers()) {
                player.sendSystemMessage(shoutMessage);
            }

            data.setLastShoutTime(currentTime);
            DataManager.saveJson("players", sender.getUUID().toString(), data);
        } else {
            long remainingCooldown = (cooldown - (currentTime - lastShout)) / 1000;
            sender.sendSystemMessage(Component.literal(
                    String.format("You must wait %d seconds before shouting again!", remainingCooldown)
            ).withStyle(ChatFormatting.RED));
        }
    }

    public static void sendPartySystemMessage(PartyData party, Component message, ServerPlayer sender) {
        for (ServerPlayer player : sender.getServer().getPlayerList().getPlayers()) {
            if (party.getMembers().contains(player.getUUID())) {
                player.sendSystemMessage(message);
            }
        }
    }

    public static void sendPrivateMessage(ServerPlayer sender, ServerPlayer recipient, String message) {
        ChatFormatting color = ChatFormatting.LIGHT_PURPLE;

        Component recipientMessage = Component.literal(
                String.format("[%s -> you]: %s",
                        getDisplayName(sender),
                        message
                )
        ).withStyle(color);
        recipient.sendSystemMessage(recipientMessage);

        Component senderMessage = Component.literal(
                String.format("[you -> %s]: %s",
                        getDisplayName(recipient),
                        message
                )
        ).withStyle(color);
        sender.sendSystemMessage(senderMessage);
    }

    public static void broadcastAdminMessage(ServerPlayer sender, String message) {
        ChatFormatting color = ChatFormatting.RED;
        Component adminMessage = Component.literal(
                String.format("[ADMIN] %s: %s",
                        getDisplayName(sender),
                        message
                )
        ).withStyle(color);

        for (ServerPlayer player : sender.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(adminMessage);
        }
    }

    public static void sendErrorMessage(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
    }

    public static void sendSuccessMessage(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.GREEN));
    }

    public static void sendInfoMessage(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW));
    }

    private static String getDisplayName(ServerPlayer player) {
        if (ModConfig.COMMON.enableFakenameIntegration.get()) {
            try {
                net.minecraft.nbt.CompoundTag tag = player.getPersistentData();
                if (tag.contains("fakename")) {
                    return tag.getString("fakename");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return player.getName().getString();
    }

    private static String formatDistance(double distance) {
        if (distance < 1000) {
            return String.format("%dm", (int) distance);
        } else {
            return String.format("%.1fkm", distance / 1000);
        }
    }
}