package com.syntrixcrate.model;

import org.bukkit.Material;
import java.util.List;

public class CrateType {

    private final String id;
    private final String displayName;
    private final Material blockType;
    private final int cooldown;
    private final boolean playerChoice;
    private final int rewardsShown;
    private final List<CrateReward> rewards;
    private final Material keyMaterial;
    private final String keyName;
    private final List<String> keyLore;

    public CrateType(String id, String displayName, Material blockType,
                     int cooldown, boolean playerChoice, int rewardsShown,
                     List<CrateReward> rewards,
                     Material keyMaterial, String keyName, List<String> keyLore) {
        this.id           = id;
        this.displayName  = displayName;
        this.blockType    = blockType;
        this.cooldown     = cooldown;
        this.playerChoice = playerChoice;
        this.rewardsShown = rewardsShown;
        this.rewards      = rewards;
        this.keyMaterial  = keyMaterial;
        this.keyName      = keyName;
        this.keyLore      = keyLore;
    }

    public String getId()                 { return id; }
    public String getDisplayName()        { return displayName; }
    public Material getBlockType()        { return blockType; }
    public int getCooldown()              { return cooldown; }
    public boolean isPlayerChoice()       { return playerChoice; }
    public int getRewardsShown()          { return rewardsShown; }
    public List<CrateReward> getRewards() { return rewards; }
    public Material getKeyMaterial()      { return keyMaterial; }
    public String getKeyName()            { return keyName; }
    public List<String> getKeyLore()      { return keyLore; }

    public double getTotalChance() {
        return rewards.stream().mapToDouble(CrateReward::getChance).sum();
    }
}
