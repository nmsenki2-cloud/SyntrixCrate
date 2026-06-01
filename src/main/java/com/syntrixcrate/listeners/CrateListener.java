package com.syntrixcrate.listeners;

import com.syntrixcrate.SyntrixCrate;
import com.syntrixcrate.editor.EditorGUI;
import com.syntrixcrate.gui.CrateGUI;
import com.syntrixcrate.model.CrateType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;

public class CrateListener implements Listener {

    private final SyntrixCrate plugin;
    private final CrateGUI crateGUI;
    private final EditorGUI editorGUI;

    public CrateListener(SyntrixCrate plugin) {
        this.plugin    = plugin;
        this.crateGUI  = new CrateGUI(plugin);
        this.editorGUI = new EditorGUI(plugin);
    }

    public CrateGUI getCrateGUI()   { return crateGUI; }
    public EditorGUI getEditorGUI() { return editorGUI; }

    @EventHandler
    public void onCrateClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        var loc = event.getClickedBlock().getLocation();
        if (!plugin.getCrateManager().isCrate(loc)) return;

        event.setCancelled(true);
        Player player   = event.getPlayer();
        String crateId  = plugin.getCrateManager().getCrateTypeId(loc);
        CrateType crate = plugin.getCrateManager().getCrateType(crateId);

        if (crate == null) { player.sendMessage(plugin.msg("crate-not-found")); return; }

        if (player.isSneaking() && player.hasPermission("mycrates.admin")) {
            editorGUI.openMainEditor(player, crateId);
            return;
        }

        int remaining = plugin.getCooldownManager()
                .getRemainingSeconds(player.getUniqueId(), crateId, crate.getCooldown());
        if (remaining > 0) {
            player.sendMessage(plugin.msg("on-cooldown").replace("{time}", String.valueOf(remaining)));
            return;
        }

        if (!plugin.getKeyManager().hasKey(player, crateId)) {
            player.sendMessage(plugin.msg("no-key").replace("{crate}", crate.getDisplayName()));
            return;
        }

        plugin.getKeyManager().removeKey(player, crateId);
        crateGUI.openChoiceGUI(player, crate);
        player.sendMessage(plugin.msg("crate-opened").replace("{crate}", crate.getDisplayName()));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (editorGUI.isEditing(player)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            editorGUI.handleClick(player, event.getRawSlot(), event.getInventory());
            return;
        }

        if (crateGUI.isInGUI(player)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            crateGUI.handleClick(player, event.getRawSlot(), event.getInventory());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        crateGUI.onClose(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!editorGUI.isAwaitingInput(player)) return;
        event.setCancelled(true);
        String message = event.getMessage();
        plugin.getServer().getScheduler().runTask(plugin, () ->
            editorGUI.handleChatInput(player, message));
    }
}
