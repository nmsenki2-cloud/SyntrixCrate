package com.syntrixcrate.managers;

import com.syntrixcrate.SyntrixCrate;
import com.syntrixcrate.model.CrateReward;
import com.syntrixcrate.model.CrateType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CrateManager {

    private final SyntrixCrate plugin;
    private final Map<String, CrateType> crateTypes = new LinkedHashMap<>();
    private final Map<String, String> crateLocations = new HashMap<>();

    private File locationsFile;
    private FileConfiguration locationsConfig;

    public CrateManager(SyntrixCrate plugin) {
        this.plugin = plugin;
        loadCrateTypes();
        loadLocations();
    }

    public void loadCrateTypes() {
        crateTypes.clear();
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection cratesSec = cfg.getConfigurationSection("crates");
        if (cratesSec == null) return;

        for (String id : cratesSec.getKeys(false)) {
            ConfigurationSection sec = cratesSec.getConfigurationSection(id);
            if (sec == null) continue;

            String displayName = SyntrixCrate.colorize(sec.getString("display-name", id));
            Material blockType = parseMaterial(sec.getString("block-type", "CHEST"), Material.CHEST);
            int cooldown       = sec.getInt("cooldown", 0);
            boolean choice     = sec.getBoolean("player-choice", true);
            int shown          = sec.getInt("rewards-shown", 9);

            ConfigurationSection keySec = sec.getConfigurationSection("key");
            Material keyMat  = keySec != null ? parseMaterial(keySec.getString("material"), Material.TRIPWIRE_HOOK) : Material.TRIPWIRE_HOOK;
            String keyName   = keySec != null ? SyntrixCrate.colorize(keySec.getString("name", id + " kulcs")) : id + " kulcs";
            List<String> keyLore = new ArrayList<>();
            if (keySec != null)
                keySec.getStringList("lore").forEach(l -> keyLore.add(SyntrixCrate.colorize(l)));

            List<CrateReward> rewards = new ArrayList<>();
            ConfigurationSection rewardsSec = sec.getConfigurationSection("rewards");
            if (rewardsSec != null) {
                for (String rid : rewardsSec.getKeys(false)) {
                    ConfigurationSection r = rewardsSec.getConfigurationSection(rid);
                    if (r == null) continue;
                    String rName   = SyntrixCrate.colorize(r.getString("display-name", rid));
                    Material rMat  = parseMaterial(r.getString("material"), Material.PAPER);
                    int rAmount    = r.getInt("amount", 1);
                    double rChance = r.getDouble("chance", 10);
                    boolean rEnch  = r.getBoolean("enchanted", false);
                    List<String> rLore = new ArrayList<>();
                    r.getStringList("lore").forEach(l -> rLore.add(SyntrixCrate.colorize(l)));
                    List<String> rActions = r.getStringList("actions");
                    rewards.add(new CrateReward(rid, rName, rMat, rAmount, rChance, rLore, rActions, rEnch));
                }
            }

            crateTypes.put(id, new CrateType(id, displayName, blockType,
                    cooldown, choice, shown, rewards, keyMat, keyName, keyLore));
        }
        plugin.getLogger().info(crateTypes.size() + " láda típus betöltve.");
    }

    private void loadLocations() {
        locationsFile = new File(plugin.getDataFolder(), "locations.yml");
        if (!locationsFile.exists()) {
            try { locationsFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        locationsConfig = YamlConfiguration.loadConfiguration(locationsFile);
        crateLocations.clear();
        if (locationsConfig.contains("locations")) {
            for (String key : locationsConfig.getConfigurationSection("locations").getKeys(false)) {
                crateLocations.put(key, locationsConfig.getString("locations." + key));
            }
        }
        plugin.getLogger().info(crateLocations.size() + " láda helyszín betöltve.");
    }

    public void saveLocations() {
        locationsConfig.set("locations", null);
        for (Map.Entry<String, String> e : crateLocations.entrySet()) {
            locationsConfig.set("locations." + e.getKey(), e.getValue());
        }
        try { locationsConfig.save(locationsFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private String locKey(Location loc) {
        return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public void setCrateLocation(Location loc, String crateId) {
        crateLocations.put(locKey(loc), crateId);
        saveLocations();
    }

    public void removeCrateLocation(Location loc) {
        crateLocations.remove(locKey(loc));
        saveLocations();
    }

    public boolean isCrate(Location loc)        { return crateLocations.containsKey(locKey(loc)); }
    public String getCrateTypeId(Location loc)  { return crateLocations.get(locKey(loc)); }
    public CrateType getCrateType(String id)    { return crateTypes.get(id); }
    public Map<String, CrateType> getAllCrateTypes() { return crateTypes; }

    private Material parseMaterial(String name, Material def) {
        if (name == null) return def;
        try { return Material.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return def; }
    }
}
