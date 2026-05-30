package com.syntrixcrate.gui;

import com.syntrixcrate.SyntrixCrate;
import com.syntrixcrate.managers.ActionExecutor;
import com.syntrixcrate.model.CrateReward;
import com.syntrixcrate.model.CrateType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CrateGUI {

    private final SyntrixCrate plugin;
    private final ActionExecutor actionExecutor;
    private final Map<UUID, String> openingPlayers = new HashMap<>();

    public CrateGUI(SyntrixCrate plugin) {
        this.plugin         = plugin;
        this.actionExecutor = new ActionExecutor(plugin);
    }

    public void openChoiceGUI(Player player, CrateType crate) {
        int size = calculateSize(crate.getRewards().size());
        String title = plugin.msgRaw("gui-title-select")
                .replace("{crate}", crate.getDisplayName());

        Inventory inv = Bukkit.createInventory(null, size, title);
        fillBorder(inv, size);

        List<CrateReward> rewards = crate.getRewards();
        int[] slots = getRewardSlots(size, rewards.size());

        for (int i = 0; i < rewards.size() && i < slots.length; i++) {
            inv.setItem(slots[i], buildRewardItem(rewards.get(i)));
        }

        openingPlayers.put(player.getUniqueId(), crate.getId());
        player.openInventory(inv);
    }

    public boolean handleClick(Player player, int slot, Inventory inv) {
        String crateId = openingPlayers.get(player.getUniqueId());
        if (crateId == null) return false;

        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        if (crate == null) return false;

        ItemStack clicked = inv.getItem(slot);
        if (clicked == null || clicked.getType() == Material.AIR) return false;
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return false;

        List<CrateReward> rewards = crate.getRewards();
        int[] slots = getRewardSlots(inv.getSize(), rewards.size());

        for (int i = 0; i < slots.length && i < rewards.size(); i++) {
            if (slots[i] == slot) {
                player.closeInventory();
                openingPlayers.remove(player.getUniqueId());
                giveReward(player, crate, rewards.get(i));
                return true;
            }
        }
        return false;
    }

    public void onClose(Player player) {
        openingPlayers.remove(player.getUniqueId());
    }

    public boolean isInGUI(Player player) {
        return openingPlayers.containsKey(player.getUniqueId());
    }

    private void giveReward(Player player, CrateType crate, CrateReward reward) {
        actionExecutor.execute(player, reward);

        player.sendMessage(plugin.msg("reward-received")
                .replace("{reward}", reward.getDisplayName()));

        plugin.getCooldownManager().setCooldown(player.getUniqueId(), crate.getId());

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
                player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0);
    }

    private ItemStack buildRewardItem(CrateReward reward) {
        ItemStack item = new ItemStack(reward.getMaterial(), reward.getAmount());
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(reward.getDisplayName());

        List<String> lore = new ArrayList<>(reward.getLore());
        lore.add("");
        lore.add(SyntrixCrate.colorize("&7Esély: &e" + reward.getChance() + "%"));
        lore.add(SyntrixCrate.colorize("&aKattints a kiválasztáshoz!"));
        meta.setLore(lore);

        if (reward.isEnchanted()) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorder(Inventory inv, int size) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta   = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        int rows = size / 9;
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
            inv.setItem(size - 9 + i, glass);
        }
        for (int r = 1; r < rows - 1; r++) {
            inv.setItem(r * 9, glass);
            inv.setItem(r * 9 + 8, glass);
        }
    }

    private int calculateSize(int rewardCount) {
        if (rewardCount <= 7)  return 27;
        if (rewardCount <= 16) return 45;
        return 54;
    }

    private int[] getRewardSlots(int invSize, int rewardCount) {
        List<Integer> slots = new ArrayList<>();
        int rows = invSize / 9;
        for (int r = 1; r < rows - 1; r++)
            for (int c = 1; c <= 7; c++)
                slots.add(r * 9 + c);

        int available = slots.size();
        if (rewardCount >= available)
            return slots.stream().mapToInt(i -> i).toArray();

        int offset = (available - rewardCount) / 2;
        return slots.subList(offset, offset + rewardCount)
                    .stream().mapToInt(i -> i).toArray();
    }
}
