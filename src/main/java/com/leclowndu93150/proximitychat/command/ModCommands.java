package com.leclowndu93150.proximitychat.command;

import com.leclowndu93150.proximitychat.chat.ChatManager;
import com.leclowndu93150.proximitychat.config.ModConfig;
import com.leclowndu93150.proximitychat.data.PartyData;
import com.leclowndu93150.proximitychat.data.PlayerData;
import com.leclowndu93150.proximitychat.party.PartyManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;

import java.util.List;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("proximitychat")
                .then(Commands.literal("party")
                        .then(Commands.literal("create")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(context -> createParty(context))))

                        .then(Commands.literal("invite")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> invitePlayer(context))))

                        .then(Commands.literal("join")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(context -> joinParty(context))))

                        .then(Commands.literal("leave")
                                .executes(context -> leaveParty(context)))

                        .then(Commands.literal("kick")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> kickPlayer(context))))

                        .then(Commands.literal("promote")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> promotePlayer(context))))

                        .then(Commands.literal("demote")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> demotePlayer(context))))

                        .then(Commands.literal("disband")
                                .executes(context -> disbandParty(context)))

                        .then(Commands.literal("list")
                                .executes(context -> listPartyMembers(context)))

                        .then(Commands.literal("info")
                                .executes(context -> partyInfo(context))))

                .then(Commands.literal("pc")
                        .executes(context -> togglePartyChat(context)))

                .then(Commands.literal("partychat")
                        .executes(context -> togglePartyChat(context)))

                .then(Commands.literal("shout")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> shout(context))))

                .then(Commands.literal("s")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> shout(context)))));

        dispatcher.register(Commands.literal("proximitychat")
                .then(Commands.literal("whisper")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> whisperMessage(context))))
                .then(Commands.literal("w")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> whisperMessage(context))))
                .then(Commands.literal("yell")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> yellMessage(context))))
                .then(Commands.literal("y")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> yellMessage(context)))));
    }

    private static int createParty(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String partyName = StringArgumentType.getString(context, "name");

        List<? extends String> bannedNames = ModConfig.COMMON.bannedPartyNames.get();
        if (bannedNames.contains(partyName.toLowerCase())) {
            player.sendSystemMessage(Component.literal("This party name is not allowed.").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (PartyManager.getParty(player.getUUID()).isPresent()) {
            player.sendSystemMessage(Component.literal("You must leave your current party first.").withStyle(ChatFormatting.RED));
            return 0;
        }

        return PartyManager.createParty(partyName, player.getUUID())
                .map(party -> {
                    player.sendSystemMessage(Component.literal("Party '" + partyName + "' created successfully!").withStyle(ChatFormatting.GREEN));
                    return 1;
                })
                .orElseGet(() -> {
                    player.sendSystemMessage(Component.literal("A party with that name already exists.").withStyle(ChatFormatting.RED));
                    return 0;
                });
    }

    private static int invitePlayer(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sender = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");

        return PartyManager.getParty(sender.getUUID())
                .filter(party -> PartyManager.isPlayerModerator(sender.getUUID(), party.getName()) ||
                        PartyManager.isPlayerOwner(sender.getUUID(), party.getName()))
                .map(party -> {
                    if (party.getMembers().size() >= ModConfig.COMMON.maxPartySize.get()) {
                        sender.sendSystemMessage(Component.literal("Party is full!").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    target.sendSystemMessage(Component.literal("You've been invited to join party '" + party.getName() + "'")
                            .withStyle(ChatFormatting.GREEN));
                    target.sendSystemMessage(Component.literal("Use /party join " + party.getName() + " to accept")
                            .withStyle(ChatFormatting.YELLOW));

                    sender.sendSystemMessage(Component.literal("Invited " + target.getName().getString() + " to the party")
                            .withStyle(ChatFormatting.GREEN));
                    return 1;
                })
                .orElseGet(() -> {
                    sender.sendSystemMessage(Component.literal("You must be in a party and have permission to invite players.").withStyle(ChatFormatting.RED));
                    return 0;
                });
    }

    private static int joinParty(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String partyName = StringArgumentType.getString(context, "name");

        if (PartyManager.getParty(player.getUUID()).isPresent()) {
            player.sendSystemMessage(Component.literal("You must leave your current party first.").withStyle(ChatFormatting.RED));
            return 0;
        }

        PartyData party = PartyManager.getPartyByName(partyName).orElse(null);
        if (party == null) {
            player.sendSystemMessage(Component.literal("Party not found.").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (party.getMembers().size() >= ModConfig.COMMON.maxPartySize.get()) {
            player.sendSystemMessage(Component.literal("Party is full!").withStyle(ChatFormatting.RED));
            return 0;
        }

        PartyManager.addPlayerToParty(player.getUUID(), partyName);
        player.sendSystemMessage(Component.literal("You joined party '" + partyName + "'").withStyle(ChatFormatting.GREEN));

        ChatManager.sendPartySystemMessage(party,
                Component.literal(player.getName().getString() + " joined the party").withStyle(ChatFormatting.YELLOW),
                player);
        return 1;
    }

    private static int leaveParty(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        return PartyManager.getParty(player.getUUID())
                .map(party -> {
                    String partyName = party.getName();
                    PartyManager.removePlayerFromParty(player.getUUID(), partyName);

                    player.sendSystemMessage(Component.literal("You left party '" + partyName + "'").withStyle(ChatFormatting.GREEN));

                    ChatManager.sendPartySystemMessage(party,
                            Component.literal(player.getName().getString() + " left the party").withStyle(ChatFormatting.YELLOW),
                            player);
                    return 1;
                })
                .orElseGet(() -> {
                    player.sendSystemMessage(Component.literal("You are not in a party.").withStyle(ChatFormatting.RED));
                    return 0;
                });
    }

    private static int kickPlayer(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sender = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");

        return PartyManager.getParty(sender.getUUID())
                .filter(party -> PartyManager.isPlayerModerator(sender.getUUID(), party.getName()) ||
                        PartyManager.isPlayerOwner(sender.getUUID(), party.getName()))
                .map(party -> {
                    if (!PartyManager.isPlayerInParty(target.getUUID(), party.getName())) {
                        sender.sendSystemMessage(Component.literal("That player is not in your party.").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    if (PartyManager.isPlayerOwner(target.getUUID(), party.getName())) {
                        sender.sendSystemMessage(Component.literal("You cannot kick the party owner.").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    PartyManager.removePlayerFromParty(target.getUUID(), party.getName());

                    target.sendSystemMessage(Component.literal("You were kicked from party '" + party.getName() + "'")
                            .withStyle(ChatFormatting.RED));

                    sender.sendSystemMessage(Component.literal("Kicked " + target.getName().getString() + " from the party")
                            .withStyle(ChatFormatting.GREEN));

                    ChatManager.sendPartySystemMessage(party,
                            Component.literal(target.getName().getString() + " was kicked from the party").withStyle(ChatFormatting.YELLOW),
                            sender);
                    return 1;
                })
                .orElseGet(() -> {
                    sender.sendSystemMessage(Component.literal("You must be in a party and have permission to kick players.").withStyle(ChatFormatting.RED));
                    return 0;
                });
    }

    private static int promotePlayer(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sender = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");

        return PartyManager.getParty(sender.getUUID())
                .filter(party -> PartyManager.isPlayerOwner(sender.getUUID(), party.getName()))
                .map(party -> {
                    if (!PartyManager.isPlayerInParty(target.getUUID(), party.getName())) {
                        sender.sendSystemMessage(Component.literal("That player is not in your party.").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    if (PartyManager.isPlayerModerator(target.getUUID(), party.getName())) {
                        sender.sendSystemMessage(Component.literal("That player is already a moderator.").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    PartyManager.promoteToModerator(party.getName(), target.getUUID());

                    target.sendSystemMessage(Component.literal("You were promoted to moderator in party '" + party.getName() + "'")
                            .withStyle(ChatFormatting.GREEN));

                    sender.sendSystemMessage(Component.literal("Promoted " + target.getName().getString() + " to moderator")
                            .withStyle(ChatFormatting.GREEN));

                    ChatManager.sendPartySystemMessage(party,
                            Component.literal(target.getName().getString() + " was promoted to moderator").withStyle(ChatFormatting.YELLOW),
                            sender);
                    return 1;
                })
                .orElseGet(() -> {
                    sender.sendSystemMessage(Component.literal("You must be the party owner to promote players.").withStyle(ChatFormatting.RED));
                    return 0;
                });
    }

    private static int demotePlayer(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sender = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");

        return PartyManager.getParty(sender.getUUID())
                .filter(party -> party.getOwner().equals(sender.getUUID()))
                .map(party -> {
                    if (!party.getModerators().contains(target.getUUID())) {
                        sender.sendSystemMessage(Component.literal("That player is not a moderator.").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    party.removeModerator(target.getUUID());

                    target.sendSystemMessage(Component.literal("You were demoted from moderator in party '" + party.getName() + "'")
                            .withStyle(ChatFormatting.RED));

                    sender.sendSystemMessage(Component.literal("Demoted " + target.getName().getString() + " from moderator")
                            .withStyle(ChatFormatting.GREEN));

                    ChatManager.sendPartySystemMessage(party,
                            Component.literal(target.getName().getString() + " was demoted from moderator").withStyle(ChatFormatting.YELLOW),
                            sender);
                    return 1;
                })
                .orElseGet(() -> {
                    sender.sendSystemMessage(Component.literal("You must be the party owner to demote players.").withStyle(ChatFormatting.RED));
                    return 0;
                });
    }

    private static int disbandParty(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        return PartyManager.getParty(player.getUUID())
                .filter(party -> party.getOwner().equals(player.getUUID()))
                .map(party -> {
                    String partyName = party.getName();

                    ChatManager.sendPartySystemMessage(party,
                            Component.literal("Party '" + partyName + "' has been disbanded").withStyle(ChatFormatting.RED),
                            player);

                    PartyManager.disbandParty(partyName);

                    player.sendSystemMessage(Component.literal("Party '" + partyName + "' disbanded").withStyle(ChatFormatting.GREEN));
                    return 1;
                })
                .orElseGet(() -> {
                    player.sendSystemMessage(Component.literal("You must be the party owner to disband the party.").withStyle(ChatFormatting.RED));
                    return 0;
                });
    }

    private static int listPartyMembers(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        return PartyManager.getParty(player.getUUID())
                .map(party -> {
                    StringBuilder memberList = new StringBuilder();
                    memberList.append("\nParty '").append(party.getName()).append("' members:\n");

                    String ownerName = getPlayerDisplayName(context.getSource().getServer()
                            .getPlayerList().getPlayer(party.getOwner()));
                    memberList.append("Owner: ").append(ownerName).append("\n");

                    memberList.append("Moderators: ");
                    if (party.getModerators().isEmpty()) {
                        memberList.append("None");
                    } else {
                        party.getModerators().forEach(uuid -> {
                            String modName = getPlayerDisplayName(context.getSource().getServer()
                                    .getPlayerList().getPlayer(uuid));
                            memberList.append(modName).append(", ");
                        });
                    }
                    memberList.append("\n");

                    memberList.append("Members: ");
                    party.getMembers().stream()
                            .filter(uuid -> !party.getOwner().equals(uuid) && !party.getModerators().contains(uuid))
                            .forEach(uuid -> {
                                String memberName = getPlayerDisplayName(context.getSource().getServer()
                                        .getPlayerList().getPlayer(uuid));
                                memberList.append(memberName).append(", ");
                            });

                    player.sendSystemMessage(Component.literal(memberList.toString()).withStyle(ChatFormatting.YELLOW));
                    return 1;
                })
                .orElseGet(() -> {
                    player.sendSystemMessage(Component.literal("You are not in a party.").withStyle(ChatFormatting.RED));
                    return 0;
                });
    }

    private static String getPlayerDisplayName(ServerPlayer player) {
        if (player != null && ModConfig.COMMON.enableFakenameIntegration.get()) {
            try {
                net.minecraft.nbt.CompoundTag tag = player.getPersistentData();
                if (tag.contains("fakename")) {
                    return tag.getString("fakename");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return player != null ? player.getName().getString() : "Unknown";
    }

    private static int partyInfo(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        return PartyManager.getParty(player.getUUID())
                .map(party -> {
                    Component info = Component.literal("")
                            .append(Component.literal("\nParty Information:").withStyle(ChatFormatting.GOLD))
                            .append(Component.literal("\nName: ").withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(party.getName()).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal("\nMembers: ").withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(String.valueOf(party.getMembers().size())).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" / ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(String.valueOf(ModConfig.COMMON.maxPartySize.get())).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal("\nCreated: ").withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(formatTimestamp(party.getCreationTime())).withStyle(ChatFormatting.WHITE));

                    player.sendSystemMessage(info);
                    return 1;
                })
                .orElseGet(() -> {
                    player.sendSystemMessage(Component.literal("You are not in a party.").withStyle(ChatFormatting.RED));
                    return 0;
                });
    }

    private static int togglePartyChat(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        return PartyManager.getParty(player.getUUID())
                .map(party -> {
                    boolean newState = !PartyManager.isPartyChatEnabled(player.getUUID());
                    PartyManager.setPartyChat(player.getUUID(), newState);

                    player.sendSystemMessage(Component.literal("Party chat " + (newState ? "enabled" : "disabled"))
                            .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED));
                    return 1;
                })
                .orElseGet(() -> {
                    player.sendSystemMessage(Component.literal("You must be in a party to use party chat.").withStyle(ChatFormatting.RED));
                    return 0;
                });
    }

    private static int shout(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String message = StringArgumentType.getString(context, "message");

        PlayerData playerData = PartyManager.getOrCreatePlayerData(player.getUUID());
        long currentTime = System.currentTimeMillis();
        long lastShout = playerData.getLastShoutTime();
        int cooldown = ModConfig.COMMON.shoutCooldown.get() * 1000;

        if (currentTime - lastShout >= cooldown) {
            ChatManager.sendShoutMessage(player, message);
            playerData.setLastShoutTime(currentTime);
            return 1;
        } else {
            long remainingSeconds = (cooldown - (currentTime - lastShout)) / 1000;
            player.sendSystemMessage(Component.literal(
                    String.format("You must wait %d seconds before shouting again!", remainingSeconds)
            ).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int whisperMessage(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String message = StringArgumentType.getString(context, "message");
        ChatManager.sendRangedMessage(player, message, ChatManager.ChatRange.WHISPER);
        return 1;
    }

    private static int yellMessage(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String message = StringArgumentType.getString(context, "message");

        PlayerData playerData = PartyManager.getOrCreatePlayerData(player.getUUID());
        long currentTime = System.currentTimeMillis();
        long lastYell = playerData.getLastShoutTime();
        int cooldown = ModConfig.COMMON.yellCooldown.get() * 1000;

        if (currentTime - lastYell >= cooldown) {
            ChatManager.sendRangedMessage(player, message, ChatManager.ChatRange.YELL);
            playerData.setLastShoutTime(currentTime);
            return 1;
        } else {
            long remainingSeconds = (cooldown - (currentTime - lastYell)) / 1000;
            player.sendSystemMessage(Component.literal(
                    String.format("You must wait %d seconds before yelling again!", remainingSeconds)
            ).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static String formatTimestamp(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date(timestamp));
    }
}