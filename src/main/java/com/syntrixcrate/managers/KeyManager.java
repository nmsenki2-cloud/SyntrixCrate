package com.syntrixcrate.managers;

import com.syntrixcrate.SyntrixCrate;
import com.syntrixcrate.model.CrateType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class KeyManager {

    private final SyntrixCrate plugin;
    private final NamespacedKey KEY_TAG;

    public KeyManager(SyntrixCrate plugin) {
        this.plugin  = plugin;
        this.KEY_TAG = new NamespacedKey(plugin, "crate_key_type");
    }

    public ItemStack createKey(CrateType crate) {
        ItemStack item = new ItemStack(crate.getKeyMaterial(), 1);
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(crate.getKeyName());
        meta.setLore(new ArrayList<>(crate.getKeyLore()));
        meta.getPersistentDataContainer().set(KEY_TAG, PersistentDataType.STRING, crate.getId());
        item.setItemMeta(meta);
        return item;
    }

    public boolean hasKey(Player player, String crateId) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isKey(item, crateId)) return true;
        }
        return false;
    }

    public boolean removeKey(Player player, String crateId) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (isKey(contents[i], crateId)) {
                if (contents[i].getAmount() > 1) {
                    contents[i].setAmount(contents[i].getAmount() - 1);
                } else {
                    player.getInventory().setItem(i, null);
                }
                return true;
            }
        }
        return false;
    }

    public void giveKey(Player player, String crateId, int amount) {
        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        if (crate == null) return;
        int remaining = amount;
        while (remaining > 0) {
            int stack = Math.min(remaining, 64);
            ItemStack key = createKey(crate);
            key.setAmount(stack);
            player.getInventory().addItem(key);
            remaining -= stack;
        }
    }

    public boolean isKey(ItemStack item, String crateId) {
        if (item == null || !item.hasItemMeta()) return false;
        String tag = item.getItemMeta().getPersistentDataContainer()
                .get(KEY_TAG, PersistentDataType.STRING);
        return crateId.equals(tag);
    }

    public String getCrateIdFromKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(KEY_TAG, PersistentDataType.STRING);
    }
}
