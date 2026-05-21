package net.hexapro.hexaItems.util;

import net.hexapro.hexaItems.HexaItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    // Map of Player UUID → (key → expiry timestamp)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    // get remaining time in seconds for a specific key
    public long getRemainingTime(UUID playerUuid, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUuid);
        if (playerCooldowns == null) return 0;
        Long expiry = playerCooldowns.get(key);
        if (expiry == null) return 0;
        return Math.max(0, (expiry - System.currentTimeMillis()) / 1000);
    }

    // check if player is on cooldown for a specific key
    public boolean isOnCooldown(UUID playerUuid, String key) {
        return getRemainingTime(playerUuid, key) > 0;
    }

    public synchronized boolean trySetCooldown(UUID playerUuid, String key, long seconds) {
        if (isOnCooldown(playerUuid, key)) return false;
        cooldowns
                .computeIfAbsent(playerUuid, k -> new HashMap<>())
                .put(key, System.currentTimeMillis() + (seconds * 1000L));
        return true;
    }

    // clear all cooldowns for a player (on logout etc)
    public void clearCooldowns(UUID playerUuid) {
        cooldowns.remove(playerUuid);
    }
}