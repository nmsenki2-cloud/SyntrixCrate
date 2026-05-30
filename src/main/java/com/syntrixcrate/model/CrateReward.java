package com.syntrixcrate.model;

import org.bukkit.Material;
import java.util.List;

public class CrateReward {

    private final String id;
    private final String displayName;
    private final Material material;
    private final int amount;
    private final double chance;
    private final List<String> lore;
    private final List<String> actions;
    private final boolean enchanted;

    public CrateReward(String id, String displayName, Material material,
                       int amount, double chance, List<String> lore,
                       List<String> actions, boolean enchanted) {
        this.id          = id;
        this.displayName = displayName;
        this.material    = material;
        this.amount      = amount;
        this.chance      = chance;
        this.lore        = lore;
        this.actions     = actions;
        this.enchanted   = enchanted;
    }

    public String getId()            { return id; }
    public String getDisplayName()   { return displayName; }
    public Material getMaterial()    { return material; }
    public int getAmount()           { return amount; }
    public double getChance()        { return chance; }
    public List<String> getLore()    { return lore; }
    public List<String> getActions() { return actions; }
    public boolean isEnchanted()     { return enchanted; }
}
