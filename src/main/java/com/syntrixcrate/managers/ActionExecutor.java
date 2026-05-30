package com.syntrixcrate.managers;

import com.syntrixcrate.SyntrixCrate;
import com.syntrixcrate.model.CrateReward;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ActionExecutor {

    private final SyntrixCrate plugin;

    public ActionExecutor(SyntrixCrate plugin) {
        this.plugin = plugin;
    }

    public void execute(Player player, CrateReward reward) {
        for (String action : reward.getActions()) {
            String a = action.trim().replace("{player}", player.getName());

            if (a.startsWith("[GIVE] ")) {
                handleGive(player, a.substring(7).trim());
            } else if (a.startsWith("[GIVE_ENCHANTED] ")) {
                handleGiveEnchanted(player, a.substring(17).trim());
            } else if (a.startsWith("[CONSOLE] ")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), a.substring(10).trim());
            } else if (a.startsWith("[PLAYER] ")) {
                player.performCommand(a.substring(9).trim());
            } else if (a.startsWith("[MESSAGE] ")) {
                player.sendMessage(SyntrixCrate.colorize(a.substring(10).trim()));
            } else if (a.startsWith("[TITLE] ")) {
                handleTitle(player, a.substring(8).trim());
            } else if (a.startsWith("[BROADCAST] ")) {
                Bukkit.broadcastMessage(SyntrixCrate.colorize(a.substring(12).trim()));
            }
        }
    }

    private void handleGive(Player player, String args) {
        String[] parts = args.split(" ");
        if (parts.length < 2) return;
        try {
            Material mat = Material.valueOf(parts[0].toUpperCase());
            int amount   = Integer.parseInt(parts[1]);
            player.getInventory().addItem(new ItemStack(mat, amount));
        } catch (Exception ignored) {}
    }

    private void handleGiveEnchanted(Player player, String args) {
        String[] parts = args.split(" ");
        if (parts.length < 2) return;
        try {
            Material mat   = Material.valueOf(parts[0].toUpperCase());
            int amount     = Integer.parseInt(parts[1]);
            ItemStack item = new ItemStack(mat, amount);
            ItemMeta meta  = item.getItemMeta();
            if (parts.length >= 3) {
                for (String enchStr : parts[2].split(",")) {
                    String[] ep = enchStr.split(":");
                    Enchantment ench = Enchantment.getByName(ep[0].toUpperCase());
                    int level = ep.length > 1 ? Integer.parseInt(ep[1]) : 1;
                    if (ench != null) meta.addEnchant(ench, level, true);
                }
            }
            item.setItemMeta(meta);
            player.getInventory().addItem(item);
        } catch (Exception ignored) {}
    }

    private void handleTitle(Player player, String args) {
        String[] parts  = args.split(";");
        String title    = parts.length > 0 ? SyntrixCrate.colorize(parts[0]) : "";
        String subtitle = parts.length > 1 ? SyntrixCrate.colorize(parts[1]) : "";
        player.sendTitle(title, subtitle, 10, 60, 20);
    }
}
