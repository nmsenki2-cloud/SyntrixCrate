package com.syntrixcrate.managers;

import com.syntrixcrate.SyntrixCrate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final SyntrixCrate plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CooldownManager(SyntrixCrate plugin) {
        this.plugin = plugin;
    }

    public int getRemainingSeconds(UUID uuid, String crateId, int cooldownSec) {
        if (cooldownSec <= 0) return 0;
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null || !map.containsKey(crateId)) return 0;
        long elapsed  = (System.currentTimeMillis() - map.get(crateId)) / 1000;
        return Math.max(0, cooldownSec - (int) elapsed);
    }

    public boolean isOnCooldown(UUID uuid, String crateId, int cooldownSec) {
        return getRemainingSeconds(uuid, crateId, cooldownSec) > 0;
    }

    public void setCooldown(UUID uuid, String crateId) {
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                 .put(crateId, System.currentTimeMillis());
    }

    public void clearCooldown(UUID uuid, String crateId) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map != null) map.remove(crateId);
    }
}
