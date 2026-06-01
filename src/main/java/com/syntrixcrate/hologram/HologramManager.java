package com.syntrixcrate.hologram;

import com.syntrixcrate.SyntrixCrate;
import com.syntrixcrate.model.CrateType;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class HologramManager {

    private final SyntrixCrate plugin;
    private boolean decentAvailable = false;
    private final Map<String, String> activeHolograms = new HashMap<>();

    public HologramManager(SyntrixCrate plugin) {
        this.plugin = plugin;
        if (Bukkit.getPluginManager().getPlugin("DecentHolograms") != null) {
            decentAvailable = true;
            plugin.getLogger().info("DecentHolograms megtalálva - hologramok aktívak!");
        } else {
            plugin.getLogger().warning("DecentHolograms nem található - hologramok kikapcsolva.");
        }
    }

    public void createHologram(Location loc, CrateType crate) {
        if (!decentAvailable || !crate.isHologramEnabled()) return;
        String name = "syntrix_" + crate.getId() + "_" + locShort(loc);
        removeHologramByName(name);

        try {
            Class<?> dhApi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            Location holoLoc = loc.clone().add(0.5, crate.getHologramHeight() + 1.0, 0.5);
            Object hologram = dhApi.getMethod("createHologram", String.class, Location.class, boolean.class)
                    .invoke(null, name, holoLoc, false);
            for (String line : crate.getHologramLines()) {
                dhApi.getMethod("addHologramLine", hologram.getClass(), String.class)
                        .invoke(null, hologram, line);
            }
            activeHolograms.put(locKey(loc), name);
        } catch (Exception e) {
            plugin.getLogger().warning("Hologram létrehozása sikertelen: " + e.getMessage());
        }
    }

    public void removeHologram(Location loc) {
        if (!decentAvailable) return;
        String name = activeHolograms.remove(locKey(loc));
        if (name != null) removeHologramByName(name);
    }

    private void removeHologramByName(String name) {
        try {
            Class<?> dhApi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            dhApi.getMethod("removeHologram", String.class).invoke(null, name);
        } catch (Exception ignored) {}
    }

    public void updateHologram(Location loc, CrateType crate) {
        removeHologram(loc);
        createHologram(loc, crate);
    }

    public void removeAll() {
        if (!decentAvailable) return;
        new HashMap<>(activeHolograms).values().forEach(this::removeHologramByName);
        activeHolograms.clear();
    }

    public void loadAll() {
        if (!decentAvailable) return;
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            plugin.getCrateManager().getAllCrateTypes().forEach((id, crate) ->
                plugin.getCrateManager().getLocationsForCrate(id).forEach((key, loc) ->
                    createHologram(loc, crate))), 40L);
    }

    public boolean isAvailable() { return decentAvailable; }

    private String locKey(Location loc) {
        return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    private String locShort(Location loc) {
        return loc.getBlockX() + "x" + loc.getBlockY() + "y" + loc.getBlockZ() + "z";
    }
}
