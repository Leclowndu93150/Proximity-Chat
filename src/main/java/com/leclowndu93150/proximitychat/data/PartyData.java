package com.leclowndu93150.proximitychat.data;

import java.util.*;

public class PartyData {
    private final String name;
    private UUID owner;
    private final Set<UUID> members;
    private final Set<UUID> moderators;
    private final long creationTime;

    public PartyData(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members = new HashSet<>();
        this.moderators = new HashSet<>();
        this.creationTime = System.currentTimeMillis();
        this.members.add(owner);
    }

    public String getName() { return name; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }
    public Set<UUID> getMembers() { return Collections.unmodifiableSet(members); }
    public Set<UUID> getModerators() { return Collections.unmodifiableSet(moderators); }
    public long getCreationTime() { return creationTime; }

    public void addMember(UUID player) {
        members.add(player);
    }

    public void removeMember(UUID player) {
        members.remove(player);
        moderators.remove(player);
        if (player.equals(owner) && !members.isEmpty()) {
            owner = members.iterator().next();
        }
    }

    public void addModerator(UUID player) {
        if (members.contains(player)) {
            moderators.add(player);
        }
    }

    public void removeModerator(UUID player) {
        moderators.remove(player);
    }

    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    public boolean isModerator(UUID player) {
        return moderators.contains(player) || owner.equals(player);
    }

    public boolean isOwner(UUID player) {
        return owner.equals(player);
    }

    public boolean hasPermission(UUID player) {
        return isModerator(player) || isOwner(player);
    }

    public int getMemberCount() {
        return members.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartyData partyData = (PartyData) o;
        return Objects.equals(name, partyData.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "PartyData{" +
                "name='" + name + '\'' +
                ", owner=" + owner +
                ", memberCount=" + members.size() +
                ", moderatorCount=" + moderators.size() +
                '}';
    }
}