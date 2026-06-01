package com.syntrixcrate.model;

import org.bukkit.Material;
import java.util.List;

public class CrateType {

    private final String id;
    private String displayName;
    private final Material blockType;
    private int cooldown;
    private final boolean playerChoice;
    private final int rewardsShown;
    private List<CrateReward> rewards;
    private Material keyMaterial;
    private String keyName;
    private List<String> keyLore;
    private boolean hologramEnabled;
    private List<String> hologramLines;
    private double hologramHeight;
    private String animationType;
    private String animParticle;
    private int animParticleCount;

    public CrateType(String id, String displayName, Material blockType,
                     int cooldown, boolean playerChoice, int rewardsShown,
                     List<CrateReward> rewards,
                     Material keyMaterial, String keyName, List<String> keyLore,
                     boolean hologramEnabled, List<String> hologramLines, double hologramHeight,
                     String animationType, String animParticle, int animParticleCount) {
        this.id                = id;
        this.displayName       = displayName;
        this.blockType         = blockType;
        this.cooldown          = cooldown;
        this.playerChoice      = playerChoice;
        this.rewardsShown      = rewardsShown;
        this.rewards           = rewards;
        this.keyMaterial       = keyMaterial;
        this.keyName           = keyName;
        this.keyLore           = keyLore;
        this.hologramEnabled   = hologramEnabled;
        this.hologramLines     = hologramLines;
        this.hologramHeight    = hologramHeight;
        this.animationType     = animationType;
        this.animParticle      = animParticle;
        this.animParticleCount = animParticleCount;
    }

    public String getId()                  { return id; }
    public String getDisplayName()         { return displayName; }
    public Material getBlockType()         { return blockType; }
    public int getCooldown()               { return cooldown; }
    public boolean isPlayerChoice()        { return playerChoice; }
    public int getRewardsShown()           { return rewardsShown; }
    public List<CrateReward> getRewards()  { return rewards; }
    public Material getKeyMaterial()       { return keyMaterial; }
    public String getKeyName()             { return keyName; }
    public List<String> getKeyLore()       { return keyLore; }
    public boolean isHologramEnabled()     { return hologramEnabled; }
    public List<String> getHologramLines() { return hologramLines; }
    public double getHologramHeight()      { return hologramHeight; }
    public String getAnimationType()       { return animationType; }
    public String getAnimParticle()        { return animParticle; }
    public int getAnimParticleCount()      { return animParticleCount; }
    public double getTotalChance()         { return rewards.stream().mapToDouble(CrateReward::getChance).sum(); }

    public void setDisplayName(String s)         { this.displayName = s; }
    public void setCooldown(int c)               { this.cooldown = c; }
    public void setRewards(List<CrateReward> r)  { this.rewards = r; }
    public void setKeyMaterial(Material m)       { this.keyMaterial = m; }
    public void setKeyName(String n)             { this.keyName = n; }
    public void setKeyLore(List<String> l)       { this.keyLore = l; }
    public void setHologramEnabled(boolean b)    { this.hologramEnabled = b; }
    public void setHologramLines(List<String> l) { this.hologramLines = l; }
    public void setHologramHeight(double h)      { this.hologramHeight = h; }
    public void setAnimationType(String t)       { this.animationType = t; }
    public void setAnimParticle(String p)        { this.animParticle = p; }
    public void setAnimParticleCount(int c)      { this.animParticleCount = c; }
}
