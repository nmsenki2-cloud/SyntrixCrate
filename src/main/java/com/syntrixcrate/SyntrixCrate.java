package com.syntrixcrate;

import com.syntrixcrate.animation.AnimationManager;
import com.syntrixcrate.commands.CratesCommand;
import com.syntrixcrate.hologram.HologramManager;
import com.syntrixcrate.listeners.CrateListener;
import com.syntrixcrate.managers.CrateManager;
import com.syntrixcrate.managers.CooldownManager;
import com.syntrixcrate.managers.KeyManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SyntrixCrate extends JavaPlugin {

    private static SyntrixCrate instance;
    private CrateManager crateManager;
    private KeyManager keyManager;
    private CooldownManager cooldownManager;
    private HologramManager hologramManager;
    private AnimationManager animationManager;
    private CrateListener crateListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.crateManager     = new CrateManager(this);
        this.keyManager       = new KeyManager(this);
        this.cooldownManager  = new CooldownManager(this);
        this.hologramManager  = new HologramManager(this);
        this.animationManager = new AnimationManager(this);

        this.crateListener = new CrateListener(this);
        getServer().getPluginManager().registerEvents(crateListener, this);

        CratesCommand cmd = new CratesCommand(this);
        getCommand("crates").setExecutor(cmd);
        getCommand("crates").setTabCompleter(cmd);

        hologramManager.loadAll();
        animationManager.loadAll();

        getLogger().info("SyntrixCrate betöltve! Ládák: " + crateManager.getAllCrateTypes().size());
    }

    @Override
    public void onDisable() {
        crateManager.saveLocations();
        hologramManager.removeAll();
        animationManager.stopAll();
        getLogger().info("SyntrixCrate leállt.");
    }

    public static SyntrixCrate getInstance()       { return instance; }
    public CrateManager getCrateManager()          { return crateManager; }
    public KeyManager getKeyManager()              { return keyManager; }
    public CooldownManager getCooldownManager()    { return cooldownManager; }
    public HologramManager getHologramManager()    { return hologramManager; }
    public AnimationManager getAnimationManager()  { return animationManager; }
    public CrateListener getCrateListener()        { return crateListener; }

    public String msg(String path) {
        String prefix = getConfig().getString("messages.prefix", "&8[&6SyntrixCrate&8] &r");
        String raw    = getConfig().getString("messages." + path, "&c[hiányzó: " + path + "]");
        return colorize(prefix + raw);
    }

    public String msgRaw(String path) {
        return colorize(getConfig().getString("messages." + path, ""));
    }

    public static String colorize(String s) {
        if (s == null) return "";
        return s.replace("&", "\u00a7");
    }
}
