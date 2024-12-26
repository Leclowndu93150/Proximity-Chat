package com.leclowndu93150.proximitychat.party;

import com.leclowndu93150.proximitychat.data.DataManager;
import com.leclowndu93150.proximitychat.data.PartyData;
import com.leclowndu93150.proximitychat.data.PlayerData;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PartyManager {
    private static final Map<String, PartyData> parties = new HashMap<>();
    private static final Map<UUID, PlayerData> playerData = new HashMap<>();
    private static MinecraftServer server;

    public static void init(MinecraftServer server) {
        PartyManager.server = server;
        loadData();
    }

    private static void loadData() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                DataManager.getModDirectory().resolve("parties"))) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".json")) {
                    String partyName = fileName.substring(0, fileName.length() - 5);
                    PartyData party = DataManager.loadJson("parties", partyName, PartyData.class);
                    if (party != null) {
                        parties.put(partyName, party);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveAll() {
        for (Map.Entry<String, PartyData> entry : parties.entrySet()) {
            DataManager.saveJson("parties", entry.getKey(), entry.getValue());
        }
        for (Map.Entry<UUID, PlayerData> entry : playerData.entrySet()) {
            DataManager.saveJson("players", entry.getKey().toString(), entry.getValue());
        }
    }

    public static Optional<PartyData> createParty(String name, UUID owner) {
        if (!parties.containsKey(name)) {
            PartyData party = new PartyData(name, owner);
            parties.put(name, party);

            PlayerData data = getOrCreatePlayerData(owner);
            data.setCurrentParty(name);

            DataManager.saveJson("parties", name, party);
            DataManager.saveJson("players", owner.toString(), data);

            return Optional.of(party);
        }
        return Optional.empty();
    }

    public static void disbandParty(String name) {
        PartyData party = parties.remove(name);
        if (party != null) {
            for (UUID member : party.getMembers()) {
                PlayerData data = playerData.get(member);
                if (data != null && name.equals(data.getCurrentParty())) {
                    data.setCurrentParty(null);
                    DataManager.saveJson("players", member.toString(), data);
                }
            }

            try {
                Files.deleteIfExists(
                        DataManager.getModDirectory()
                                .resolve("parties")
                                .resolve(name + ".json")
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Optional<PartyData> getPartyByName(String name) {
        return Optional.ofNullable(parties.get(name));
    }

    public static void addPlayerToParty(UUID playerId, String partyName) {
        PartyData party = parties.get(partyName);
        if (party != null) {
            party.addMember(playerId);
            PlayerData data = getOrCreatePlayerData(playerId);
            data.setCurrentParty(partyName);

            DataManager.saveJson("parties", partyName, party);
            DataManager.saveJson("players", playerId.toString(), data);
        }
    }

    public static void removePlayerFromParty(UUID playerId, String partyName) {
        PartyData party = parties.get(partyName);
        if (party != null) {
            party.removeMember(playerId);
            PlayerData data = getOrCreatePlayerData(playerId);
            data.setCurrentParty(null);
            data.setPartyChatEnabled(false);

            if (party.getMembers().isEmpty()) {
                disbandParty(partyName);
            } else {
                if (party.getOwner().equals(playerId) && !party.getMembers().isEmpty()) {
                    UUID newOwner = party.getMembers().iterator().next();
                    party.setOwner(newOwner);
                }
                DataManager.saveJson("parties", partyName, party);
            }

            DataManager.saveJson("players", playerId.toString(), data);
        }
    }

    public static void promoteToModerator(String partyName, UUID playerId) {
        PartyData party = parties.get(partyName);
        if (party != null && party.getMembers().contains(playerId)) {
            party.addModerator(playerId);
            DataManager.saveJson("parties", partyName, party);
        }
    }

    public static void demoteFromModerator(String partyName, UUID playerId) {
        PartyData party = parties.get(partyName);
        if (party != null) {
            party.removeModerator(playerId);
            DataManager.saveJson("parties", partyName, party);
        }
    }

    public static boolean isPlayerInParty(UUID playerId, String partyName) {
        PartyData party = parties.get(partyName);
        return party != null && party.getMembers().contains(playerId);
    }

    public static boolean isPlayerModerator(UUID playerId, String partyName) {
        PartyData party = parties.get(partyName);
        return party != null && party.getModerators().contains(playerId);
    }

    public static boolean isPlayerOwner(UUID playerId, String partyName) {
        PartyData party = parties.get(partyName);
        return party != null && party.getOwner().equals(playerId);
    }

    public static Set<String> getPartyNames() {
        return new HashSet<>(parties.keySet());
    }

    public static PlayerData getOrCreatePlayerData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, id -> {
            PlayerData data = DataManager.loadJson(
                    "players",
                    id.toString(),
                    PlayerData.class
            );
            return data != null ? data : new PlayerData(id);
        });
    }

    public static void setPartyChat(UUID playerId, boolean enabled) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.setPartyChatEnabled(enabled);
        DataManager.saveJson("players", playerId.toString(), data);
    }

    public static boolean isPartyChatEnabled(UUID playerId) {
        return getOrCreatePlayerData(playerId).isPartyChatEnabled();
    }

    public static Optional<PartyData> getParty(UUID playerId) {
        PlayerData data = getOrCreatePlayerData(playerId);
        String partyName = data.getCurrentParty();
        return Optional.ofNullable(parties.get(partyName));
    }
}