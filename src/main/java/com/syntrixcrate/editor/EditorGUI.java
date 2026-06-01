package com.syntrixcrate.editor;

import com.syntrixcrate.SyntrixCrate;
import com.syntrixcrate.model.CrateReward;
import com.syntrixcrate.model.CrateType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EditorGUI {

    private final SyntrixCrate plugin;
    private final Map<UUID, String> editingCrate   = new HashMap<>();
    private final Map<UUID, String> currentScreen  = new HashMap<>();
    private final Map<UUID, Integer> editingReward = new HashMap<>();
    private final Map<UUID, String> awaitingInput  = new HashMap<>();

    public EditorGUI(SyntrixCrate plugin) {
        this.plugin = plugin;
    }

    public void openMainEditor(Player player, String crateId) {
        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        if (crate == null) { player.sendMessage(plugin.msg("invalid-crate").replace("{crate}", crateId)); return; }

        editingCrate.put(player.getUniqueId(), crateId);
        currentScreen.put(player.getUniqueId(), "MAIN");

        Inventory inv = Bukkit.createInventory(null, 54,
                SyntrixCrate.colorize("&8Editor: &6" + crate.getDisplayName()));
        fillGlass(inv, Material.BLACK_STAINED_GLASS_PANE);

        inv.setItem(10, makeItem(Material.CHEST, "&aJutalmak szerkesztése",
                Arrays.asList("&7Jutalmak: &e" + crate.getRewards().size(), "&7Kattints a szerkesztéshez")));
        inv.setItem(12, makeItem(Material.CLOCK, "&eCooldown / Delay",
                Arrays.asList("&7Jelenlegi: &e" + crate.getCooldown() + " mp", "&7Kattints a módosításhoz")));
        inv.setItem(14, makeItem(crate.getKeyMaterial(), "&bKulcs megjelenése",
                Arrays.asList("&7Név: " + crate.getKeyName(), "&7Kattints a módosításhoz")));
        inv.setItem(16, makeItem(Material.FIREWORK_ROCKET, "&dAnimáció beállítás",
                Arrays.asList("&7Típus: &e" + crate.getAnimationType(),
                              "&7Particle: &e" + crate.getAnimParticle(),
                              "&7Mennyiség: &e" + crate.getAnimParticleCount())));
        inv.setItem(28, makeItem(Material.NAME_TAG, "&6Hologram szöveg",
                Arrays.asList("&7Engedélyezve: &e" + crate.isHologramEnabled(),
                              "&7Sorok: &e" + crate.getHologramLines().size())));
        inv.setItem(49, makeItem(Material.LIME_DYE, "&a&lMentés & Bezárás",
                Arrays.asList("&7Elmenti az összes módosítást")));

        player.openInventory(inv);
    }

    public void openRewardList(Player player) {
        String crateId = editingCrate.get(player.getUniqueId());
        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        currentScreen.put(player.getUniqueId(), "REWARDS");

        Inventory inv = Bukkit.createInventory(null, 54,
                SyntrixCrate.colorize("&8Jutalmak - &6" + crate.getDisplayName()));
        fillGlass(inv, Material.GRAY_STAINED_GLASS_PANE);

        List<CrateReward> rewards = crate.getRewards();
        for (int i = 0; i < rewards.size() && i < 45; i++) {
            CrateReward r = rewards.get(i);
            inv.setItem(i, makeItem(r.getMaterial(), r.getDisplayName(),
                    Arrays.asList("&7Esély: &e" + r.getChance() + "%",
                                  "&7Mennyiség: &e" + r.getAmount(),
                                  "", "&aBal klikk: &7Szerkesztés",
                                  "&cJobb klikk: &7Törlés")));
        }
        inv.setItem(49, makeItem(Material.ARROW, "&cVissza", Collections.emptyList()));
        player.openInventory(inv);
    }

    public void openRewardEdit(Player player, int rewardIndex) {
        String crateId = editingCrate.get(player.getUniqueId());
        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        List<CrateReward> rewards = crate.getRewards();
        if (rewardIndex >= rewards.size()) return;

        editingReward.put(player.getUniqueId(), rewardIndex);
        currentScreen.put(player.getUniqueId(), "REWARD_EDIT");

        CrateReward reward = rewards.get(rewardIndex);
        Inventory inv = Bukkit.createInventory(null, 54,
                SyntrixCrate.colorize("&8Szerkesztés: " + reward.getDisplayName()));
        fillGlass(inv, Material.GRAY_STAINED_GLASS_PANE);

        inv.setItem(4, makeItem(reward.getMaterial(), reward.getDisplayName(),
                Arrays.asList("&7Esély: &e" + reward.getChance() + "%",
                              "&7Mennyiség: &e" + reward.getAmount())));

        inv.setItem(19, makeItem(Material.RED_DYE,  "&c- 10%", Collections.singletonList("&7Csökkentés")));
        inv.setItem(20, makeItem(Material.RED_DYE,  "&c- 5%",  Collections.singletonList("&7Csökkentés")));
        inv.setItem(21, makeItem(Material.RED_DYE,  "&c- 1%",  Collections.singletonList("&7Csökkentés")));
        inv.setItem(22, makeItem(Material.PAPER, "&eJelenlegi esély",
                Arrays.asList("&a" + reward.getChance() + "%", "", "&7Kattints: közvetlen beírás chatben")));
        inv.setItem(23, makeItem(Material.LIME_DYE, "&a+ 1%",  Collections.singletonList("&7Növelés")));
        inv.setItem(24, makeItem(Material.LIME_DYE, "&a+ 5%",  Collections.singletonList("&7Növelés")));
        inv.setItem(25, makeItem(Material.LIME_DYE, "&a+ 10%", Collections.singletonList("&7Növelés")));

        inv.setItem(37, makeItem(Material.BARRIER, "&c&lTörlés",
                Arrays.asList("&cEltávolítja a jutalmat!")));
        inv.setItem(40, makeItem(Material.LIME_DYE, "&a&lMentés", Collections.singletonList("&7Elmenti")));
        inv.setItem(49, makeItem(Material.ARROW, "&cVissza", Collections.emptyList()));

        player.openInventory(inv);
    }

    public void openCooldownEditor(Player player) {
        String crateId = editingCrate.get(player.getUniqueId());
        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        currentScreen.put(player.getUniqueId(), "COOLDOWN");

        Inventory inv = Bukkit.createInventory(null, 54,
                SyntrixCrate.colorize("&8Cooldown - &6" + crate.getDisplayName()));
        fillGlass(inv, Material.GRAY_STAINED_GLASS_PANE);

        inv.setItem(4, makeItem(Material.CLOCK, "&eJelenlegi Cooldown",
                Arrays.asList("&a" + crate.getCooldown() + " másodperc", "&70 = nincs")));

        inv.setItem(19, makeItem(Material.RED_DYE,  "&c- 60mp",  Collections.emptyList()));
        inv.setItem(20, makeItem(Material.RED_DYE,  "&c- 10mp",  Collections.emptyList()));
        inv.setItem(21, makeItem(Material.RED_DYE,  "&c- 1mp",   Collections.emptyList()));
        inv.setItem(23, makeItem(Material.LIME_DYE, "&a+ 1mp",   Collections.emptyList()));
        inv.setItem(24, makeItem(Material.LIME_DYE, "&a+ 10mp",  Collections.emptyList()));
        inv.setItem(25, makeItem(Material.LIME_DYE, "&a+ 60mp",  Collections.emptyList()));

        inv.setItem(28, makeItem(Material.PAPER, "&70 mp",   Collections.emptyList()));
        inv.setItem(29, makeItem(Material.PAPER, "&75 mp",   Collections.emptyList()));
        inv.setItem(30, makeItem(Material.PAPER, "&730 mp",  Collections.emptyList()));
        inv.setItem(31, makeItem(Material.PAPER, "&760 mp",  Collections.emptyList()));
        inv.setItem(32, makeItem(Material.PAPER, "&7300 mp", Collections.emptyList()));
        inv.setItem(33, makeItem(Material.PAPER, "&71800 mp",Collections.emptyList()));

        inv.setItem(49, makeItem(Material.ARROW, "&cVissza", Collections.emptyList()));
        player.openInventory(inv);
    }

    public void openAnimationEditor(Player player) {
        String crateId = editingCrate.get(player.getUniqueId());
        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        currentScreen.put(player.getUniqueId(), "ANIMATION");

        Inventory inv = Bukkit.createInventory(null, 54,
                SyntrixCrate.colorize("&8Animáció - &6" + crate.getDisplayName()));
        fillGlass(inv, Material.GRAY_STAINED_GLASS_PANE);

        inv.setItem(4, makeItem(Material.FIREWORK_ROCKET, "&dJelenlegi animáció",
                Arrays.asList("&7Típus: &e" + crate.getAnimationType(),
                              "&7Particle: &e" + crate.getAnimParticle(),
                              "&7Mennyiség: &e" + crate.getAnimParticleCount())));

        inv.setItem(10, makeItem(Material.BARRIER,         "&cNINCS",           Collections.singletonList("&7Nincs animáció")));
        inv.setItem(11, makeItem(Material.FIREWORK_ROCKET, "&aSPINNING (forgó)", Collections.singletonList("&7Körök forognak")));

        inv.setItem(19, makeItem(Material.LIME_DYE,         "&aVILLAGER_HAPPY",  Collections.emptyList()));
        inv.setItem(20, makeItem(Material.BLAZE_POWDER,     "&6FLAME",            Collections.emptyList()));
        inv.setItem(21, makeItem(Material.SNOWBALL,         "&bSNOWFLAKE",        Collections.emptyList()));
        inv.setItem(22, makeItem(Material.HEART_OF_THE_SEA, "&dSPELL_WITCH",      Collections.emptyList()));
        inv.setItem(23, makeItem(Material.GLOWSTONE_DUST,  "&eTOTEM_OF_UNDYING", Collections.emptyList()));
        inv.setItem(24, makeItem(Material.DRAGON_BREATH,   "&5DRAGON_BREATH",    Collections.emptyList()));

        inv.setItem(28, makeItem(Material.RED_DYE,  "&c- 5 particle", Collections.emptyList()));
        inv.setItem(31, makeItem(Material.PAPER, "&eParticle: &a" + crate.getAnimParticleCount(), Collections.emptyList()));
        inv.setItem(34, makeItem(Material.LIME_DYE, "&a+ 5 particle", Collections.emptyList()));

        inv.setItem(49, makeItem(Material.ARROW, "&cVissza", Collections.emptyList()));
        player.openInventory(inv);
    }

    public void openHologramEditor(Player player) {
        String crateId = editingCrate.get(player.getUniqueId());
        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        currentScreen.put(player.getUniqueId(), "HOLOGRAM");

        Inventory inv = Bukkit.createInventory(null, 54,
                SyntrixCrate.colorize("&8Hologram - &6" + crate.getDisplayName()));
        fillGlass(inv, Material.GRAY_STAINED_GLASS_PANE);

        Material toggleMat = crate.isHologramEnabled() ? Material.LIME_DYE : Material.RED_DYE;
        String toggleName  = crate.isHologramEnabled() ? "&aHologram: BE" : "&cHologram: KI";
        inv.setItem(4, makeItem(toggleMat, toggleName, Collections.singletonList("&7Kattints a be/kikapcsoláshoz")));

        List<String> lines = crate.getHologramLines();
        for (int i = 0; i < lines.size() && i < 9; i++) {
            inv.setItem(19 + i, makeItem(Material.NAME_TAG,
                    "&7" + (i + 1) + ". sor: &f" + lines.get(i),
                    Arrays.asList("&aBal klikk: &7Szerkesztés (chatben)",
                                  "&cJobb klikk: &7Sor törlése")));
        }

        inv.setItem(37, makeItem(Material.LIME_DYE, "&a+ Új sor", Collections.singletonList("&7Chatben írd be")));
        inv.setItem(39, makeItem(Material.RED_DYE,  "&c- Magasság", Collections.singletonList("&7" + crate.getHologramHeight())));
        inv.setItem(40, makeItem(Material.PAPER, "&eMagasság: &a" + crate.getHologramHeight(), Collections.emptyList()));
        inv.setItem(41, makeItem(Material.LIME_DYE, "&a+ Magasság", Collections.singletonList("&7" + crate.getHologramHeight())));
        inv.setItem(49, makeItem(Material.ARROW, "&cVissza", Collections.emptyList()));

        player.openInventory(inv);
    }

    public void openKeyEditor(Player player) {
        String crateId = editingCrate.get(player.getUniqueId());
        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        currentScreen.put(player.getUniqueId(), "KEY");

        Inventory inv = Bukkit.createInventory(null, 54,
                SyntrixCrate.colorize("&8Kulcs - &6" + crate.getDisplayName()));
        fillGlass(inv, Material.GRAY_STAINED_GLASS_PANE);

        inv.setItem(4, makeItem(crate.getKeyMaterial(), crate.getKeyName(), crate.getKeyLore()));
        inv.setItem(19, makeItem(Material.NAME_TAG, "&aNév szerkesztése",
                Arrays.asList("&7Jelenlegi: " + crate.getKeyName(), "&7Kattints és írd be chatben")));
        inv.setItem(21, makeItem(Material.TRIPWIRE_HOOK,  "&7TRIPWIRE_HOOK",  Collections.emptyList()));
        inv.setItem(22, makeItem(Material.NETHER_STAR,    "&7NETHER_STAR",    Collections.emptyList()));
        inv.setItem(23, makeItem(Material.GOLD_NUGGET,    "&7GOLD_NUGGET",    Collections.emptyList()));
        inv.setItem(24, makeItem(Material.DIAMOND,        "&7DIAMOND",        Collections.emptyList()));
        inv.setItem(25, makeItem(Material.EMERALD,        "&7EMERALD",        Collections.emptyList()));
        inv.setItem(49, makeItem(Material.ARROW, "&cVissza", Collections.emptyList()));

        player.openInventory(inv);
    }

    public boolean handleClick(Player player, int slot, Inventory inv) {
        UUID uuid      = player.getUniqueId();
        String screen  = currentScreen.getOrDefault(uuid, "");
        String crateId = editingCrate.get(uuid);
        if (crateId == null) return false;

        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        if (crate == null) return false;

        switch (screen) {
            case "MAIN"        -> handleMainClick(player, slot, crate);
            case "REWARDS"     -> handleRewardsClick(player, slot, crate);
            case "REWARD_EDIT" -> handleRewardEditClick(player, slot, crate);
            case "COOLDOWN"    -> handleCooldownClick(player, slot, crate);
            case "ANIMATION"   -> handleAnimationClick(player, slot, crate);
            case "HOLOGRAM"    -> handleHologramClick(player, slot, crate);
            case "KEY"         -> handleKeyClick(player, slot, crate);
        }
        return true;
    }

    private void handleMainClick(Player player, int slot, CrateType crate) {
        switch (slot) {
            case 10 -> openRewardList(player);
            case 12 -> openCooldownEditor(player);
            case 14 -> openKeyEditor(player);
            case 16 -> openAnimationEditor(player);
            case 28 -> openHologramEditor(player);
            case 49 -> saveAndClose(player, crate);
        }
    }

    private void handleRewardsClick(Player player, int slot, CrateType crate) {
        if (slot == 49) { openMainEditor(player, crate.getId()); return; }
        if (slot < crate.getRewards().size()) openRewardEdit(player, slot);
    }

    private void handleRewardEditClick(Player player, int slot, CrateType crate) {
        int idx = editingReward.getOrDefault(player.getUniqueId(), -1);
        if (idx < 0 || idx >= crate.getRewards().size()) return;

        switch (slot) {
            case 19 -> changeChance(crate, idx, -10);
            case 20 -> changeChance(crate, idx, -5);
            case 21 -> changeChance(crate, idx, -1);
            case 23 -> changeChance(crate, idx, 1);
            case 24 -> changeChance(crate, idx, 5);
            case 25 -> changeChance(crate, idx, 10);
            case 22 -> {
                player.closeInventory();
                awaitingInput.put(player.getUniqueId(), "CHANCE_" + idx);
                player.sendMessage(SyntrixCrate.colorize("&eÍrd be az új esélyt (pl. 25):"));
                return;
            }
            case 37 -> {
                List<CrateReward> rewards = new ArrayList<>(crate.getRewards());
                rewards.remove(idx);
                crate.setRewards(rewards);
                player.sendMessage(SyntrixCrate.colorize("&cJutalom törölve."));
                openRewardList(player);
                return;
            }
            case 40 -> { plugin.getCrateManager().saveCrateType(crate); player.sendMessage(plugin.msg("editor-saved")); }
            case 49 -> { openRewardList(player); return; }
        }
        openRewardEdit(player, idx);
    }

    private void handleCooldownClick(Player player, int slot, CrateType crate) {
        int cd = crate.getCooldown();
        switch (slot) {
            case 19 -> crate.setCooldown(Math.max(0, cd - 60));
            case 20 -> crate.setCooldown(Math.max(0, cd - 10));
            case 21 -> crate.setCooldown(Math.max(0, cd - 1));
            case 23 -> crate.setCooldown(cd + 1);
            case 24 -> crate.setCooldown(cd + 10);
            case 25 -> crate.setCooldown(cd + 60);
            case 28 -> crate.setCooldown(0);
            case 29 -> crate.setCooldown(5);
            case 30 -> crate.setCooldown(30);
            case 31 -> crate.setCooldown(60);
            case 32 -> crate.setCooldown(300);
            case 33 -> crate.setCooldown(1800);
            case 49 -> { openMainEditor(player, crate.getId()); return; }
        }
        openCooldownEditor(player);
    }

    private void handleAnimationClick(Player player, int slot, CrateType crate) {
        switch (slot) {
            case 10 -> crate.setAnimationType("NONE");
            case 11 -> crate.setAnimationType("SPINNING");
            case 19 -> crate.setAnimParticle("VILLAGER_HAPPY");
            case 20 -> crate.setAnimParticle("FLAME");
            case 21 -> crate.setAnimParticle("SNOWFLAKE");
            case 22 -> crate.setAnimParticle("SPELL_WITCH");
            case 23 -> crate.setAnimParticle("TOTEM_OF_UNDYING");
            case 24 -> crate.setAnimParticle("DRAGON_BREATH");
            case 28 -> crate.setAnimParticleCount(Math.max(1, crate.getAnimParticleCount() - 5));
            case 34 -> crate.setAnimParticleCount(Math.min(50, crate.getAnimParticleCount() + 5));
            case 49 -> { openMainEditor(player, crate.getId()); return; }
        }
        plugin.getAnimationManager().restartAnimationForCrate(crate.getId());
        openAnimationEditor(player);
    }

    private void handleHologramClick(Player player, int slot, CrateType crate) {
        List<String> lines = new ArrayList<>(crate.getHologramLines());

        if (slot == 4) {
            crate.setHologramEnabled(!crate.isHologramEnabled());
            plugin.getHologramManager().removeAll();
            if (crate.isHologramEnabled()) plugin.getHologramManager().loadAll();
            openHologramEditor(player);
            return;
        }

        int lineIndex = slot - 19;
        if (lineIndex >= 0 && lineIndex < lines.size()) {
            player.closeInventory();
            awaitingInput.put(player.getUniqueId(), "HOLO_EDIT_" + lineIndex);
            player.sendMessage(SyntrixCrate.colorize("&eÍrd be az új szöveget a(z) " + (lineIndex + 1) + ". sorhoz:"));
            return;
        }

        if (slot == 37) {
            player.closeInventory();
            awaitingInput.put(player.getUniqueId(), "HOLO_ADD");
            player.sendMessage(SyntrixCrate.colorize("&eÍrd be az új hologram sort:"));
            return;
        }

        if (slot == 39) crate.setHologramHeight(Math.max(0.5, crate.getHologramHeight() - 0.25));
        if (slot == 41) crate.setHologramHeight(Math.min(5.0, crate.getHologramHeight() + 0.25));
        if (slot == 49) { openMainEditor(player, crate.getId()); return; }

        openHologramEditor(player);
    }

    private void handleKeyClick(Player player, int slot, CrateType crate) {
        switch (slot) {
            case 19 -> {
                player.closeInventory();
                awaitingInput.put(player.getUniqueId(), "KEY_NAME");
                player.sendMessage(SyntrixCrate.colorize("&eÍrd be az új kulcs nevét:"));
                return;
            }
            case 21 -> crate.setKeyMaterial(Material.TRIPWIRE_HOOK);
            case 22 -> crate.setKeyMaterial(Material.NETHER_STAR);
            case 23 -> crate.setKeyMaterial(Material.GOLD_NUGGET);
            case 24 -> crate.setKeyMaterial(Material.DIAMOND);
            case 25 -> crate.setKeyMaterial(Material.EMERALD);
            case 49 -> { openMainEditor(player, crate.getId()); return; }
        }
        openKeyEditor(player);
    }

    public boolean isAwaitingInput(Player player) {
        return awaitingInput.containsKey(player.getUniqueId());
    }

    public void handleChatInput(Player player, String message) {
        UUID uuid      = player.getUniqueId();
        String field   = awaitingInput.remove(uuid);
        String crateId = editingCrate.get(uuid);
        if (field == null || crateId == null) return;

        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        if (crate == null) return;

        if (field.startsWith("CHANCE_")) {
            int idx = Integer.parseInt(field.split("_")[1]);
            try {
                double val = Double.parseDouble(message);
                changeChanceDirect(crate, idx, val);
                player.sendMessage(SyntrixCrate.colorize("&aEsély beállítva: &e" + val + "%"));
            } catch (NumberFormatException e) {
                player.sendMessage(SyntrixCrate.colorize("&cÉrvénytelen szám!"));
            }
            openRewardEdit(player, idx);

        } else if (field.startsWith("HOLO_EDIT_")) {
            int idx = Integer.parseInt(field.split("_")[2]);
            List<String> lines = new ArrayList<>(crate.getHologramLines());
            if (idx < lines.size()) {
                lines.set(idx, SyntrixCrate.colorize(message));
                crate.setHologramLines(lines);
                player.sendMessage(SyntrixCrate.colorize("&aHologram sor frissítve."));
            }
            openHologramEditor(player);

        } else if (field.equals("HOLO_ADD")) {
            List<String> lines = new ArrayList<>(crate.getHologramLines());
            lines.add(SyntrixCrate.colorize(message));
            crate.setHologramLines(lines);
            player.sendMessage(SyntrixCrate.colorize("&aÚj sor hozzáadva."));
            openHologramEditor(player);

        } else if (field.equals("KEY_NAME")) {
            crate.setKeyName(SyntrixCrate.colorize(message));
            player.sendMessage(SyntrixCrate.colorize("&aKulcs neve frissítve."));
            openKeyEditor(player);
        }
    }

    private void saveAndClose(Player player, CrateType crate) {
        plugin.getCrateManager().saveCrateType(crate);
        plugin.getCrateManager().getLocationsForCrate(crate.getId()).forEach((key, loc) -> {
            plugin.getHologramManager().updateHologram(loc, crate);
            plugin.getAnimationManager().restartAnimation(loc, crate);
        });
        player.closeInventory();
        player.sendMessage(plugin.msg("editor-saved"));
        cleanup(player);
    }

    public void cleanup(Player player) {
        UUID uuid = player.getUniqueId();
        editingCrate.remove(uuid);
        currentScreen.remove(uuid);
        editingReward.remove(uuid);
        awaitingInput.remove(uuid);
    }

    public boolean isEditing(Player player) {
        return editingCrate.containsKey(player.getUniqueId());
    }

    private void changeChance(CrateType crate, int idx, double delta) {
        List<CrateReward> rewards = crate.getRewards();
        if (idx >= rewards.size()) return;
        CrateReward old = rewards.get(idx);
        double newChance = Math.max(0.1, Math.min(100, old.getChance() + delta));
        rewards.set(idx, new CrateReward(old.getId(), old.getDisplayName(), old.getMaterial(),
                old.getAmount(), newChance, old.getLore(), old.getActions(), old.isEnchanted()));
    }

    private void changeChanceDirect(CrateType crate, int idx, double val) {
        List<CrateReward> rewards = crate.getRewards();
        if (idx >= rewards.size()) return;
        CrateReward old = rewards.get(idx);
        double newChance = Math.max(0.1, Math.min(100, val));
        rewards.set(idx, new CrateReward(old.getId(), old.getDisplayName(), old.getMaterial(),
                old.getAmount(), newChance, old.getLore(), old.getActions(), old.isEnchanted()));
    }

    private ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(SyntrixCrate.colorize(name));
        List<String> colored = new ArrayList<>();
        lore.forEach(l -> colored.add(SyntrixCrate.colorize(l)));
        meta.setLore(colored);
        item.setItemMeta(meta);
        return item;
    }

    private void fillGlass(Inventory inv, Material mat) {
        ItemStack glass = new ItemStack(mat);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
    }
}
