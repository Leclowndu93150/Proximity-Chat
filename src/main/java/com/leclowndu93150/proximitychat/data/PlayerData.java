package com.leclowndu93150.proximitychat.data;

import java.util.UUID;

public class PlayerData {
    private final UUID playerId;
    private String currentParty;
    private boolean partyChatEnabled;
    private long lastShoutTime;

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() { return playerId; }
    public String getCurrentParty() { return currentParty; }
    public void setCurrentParty(String party) { this.currentParty = party; }
    public boolean isPartyChatEnabled() { return partyChatEnabled; }
    public void setPartyChatEnabled(boolean enabled) { this.partyChatEnabled = enabled; }
    public long getLastShoutTime() { return lastShoutTime; }
    public void setLastShoutTime(long time) { this.lastShoutTime = time; }
}


