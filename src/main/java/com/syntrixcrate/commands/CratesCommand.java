package com.syntrixcrate.commands;

import com.syntrixcrate.SyntrixCrate;
import com.syntrixcrate.model.CrateType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class CratesCommand implements CommandExecutor, TabCompleter {

    private final SyntrixCrate plugin;

    public CratesCommand(SyntrixCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "set" -> {
                if (!checkAdmin(sender)) return true;
                if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékos!"); return true; }
                if (args.length < 2) { player.sendMessage(SyntrixCrate.colorize("&eHasználat: /crates set <típus>")); return true; }
                if (plugin.getCrateManager().getCrateType(args[1]) == null) {
                    player.sendMessage(plugin.msg("invalid-crate").replace("{crate}", args[1])); return true;
                }
                var block = player.getTargetBlockExact(5);
                if (block == null) { player.sendMessage(SyntrixCrate.colorize("&cNézz egy blokkra!")); return true; }
                var loc = block.getLocation();
                plugin.getCrateManager().setCrateLocation(loc, args[1]);
                CrateType crate = plugin.getCrateManager().getCrateType(args[1]);
                plugin.getHologramManager().createHologram(loc, crate);
                plugin.getAnimationManager().startAnimation(loc, crate);
                player.sendMessage(plugin.msg("crate-set")
                        .replace("{crate}", args[1])
                        .replace("{location}", block.getX() + "," + block.getY() + "," + block.getZ()));
            }
            case "remove" -> {
                if (!checkAdmin(sender)) return true;
                if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékos!"); return true; }
                var block = player.getTargetBlockExact(5);
                if (block == null || !plugin.getCrateManager().isCrate(block.getLocation())) {
                    player.sendMessage(plugin.msg("crate-not-found")); return true;
                }
                var loc = block.getLocation();
                plugin.getHologramManager().removeHologram(loc);
                plugin.getAnimationManager().stopAnimation(loc);
                plugin.getCrateManager().removeCrateLocation(loc);
                player.sendMessage(plugin.msg("crate-removed"));
            }
            case "give" -> {
                if (!checkAdmin(sender)) return true;
                if (args.length < 3) { sender.sendMessage(SyntrixCrate.colorize("&eHasználat: /crates give <játékos> <típus> [db]")); return true; }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) { sender.sendMessage(plugin.msg("player-not-found").replace("{player}", args[1])); return true; }
                if (plugin.getCrateManager().getCrateType(args[2]) == null) {
                    sender.sendMessage(plugin.msg("invalid-crate").replace("{crate}", args[2])); return true;
                }
                int amount = 1;
                if (args.length >= 4) try { amount = Math.max(1, Integer.parseInt(args[3])); } catch (NumberFormatException ignored) {}
                plugin.getKeyManager().giveKey(target, args[2], amount);
                sender.sendMessage(plugin.msg("key-given").replace("{amount}", String.valueOf(amount)).replace("{crate}", args[2]).replace("{player}", target.getName()));
                target.sendMessage(plugin.msg("key-received").replace("{amount}", String.valueOf(amount)).replace("{crate}", args[2]));
            }
            case "key" -> {
                if (!checkAdmin(sender)) return true;
                if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékos!"); return true; }
                if (args.length < 2) { player.sendMessage(SyntrixCrate.colorize("&eHasználat: /crates key <típus>")); return true; }
                if (plugin.getCrateManager().getCrateType(args[1]) == null) {
                    player.sendMessage(plugin.msg("invalid-crate").replace("{crate}", args[1])); return true;
                }
                plugin.getKeyManager().giveKey(player, args[1], 1);
                player.sendMessage(plugin.msg("key-received").replace("{amount}", "1").replace("{crate}", args[1]));
            }
            case "editor" -> {
                if (!checkAdmin(sender)) return true;
                if (!(sender instanceof Player player)) { sender.sendMessage("Csak játékos!"); return true; }
                if (args.length < 2) {
                    player.sendMessage(SyntrixCrate.colorize("&6--- Ládák ---"));
                    plugin.getCrateManager().getAllCrateTypes().keySet()
                            .forEach(id -> player.sendMessage(SyntrixCrate.colorize("&e/crates editor " + id)));
                    return true;
                }
                if (plugin.getCrateManager().getCrateType(args[1]) == null) {
                    player.sendMessage(plugin.msg("invalid-crate").replace("{crate}", args[1])); return true;
                }
                plugin.getCrateListener().getEditorGUI().openMainEditor(player, args[1]);
            }
            case "list" -> {
                if (!checkAdmin(sender)) return true;
                sender.sendMessage(SyntrixCrate.colorize("&6--- Elérhető ládák ---"));
                for (CrateType ct : plugin.getCrateManager().getAllCrateTypes().values()) {
                    sender.sendMessage(SyntrixCrate.colorize("&e" + ct.getId() + " &7- " + ct.getDisplayName() +
                            " &7(cooldown: &f" + ct.getCooldown() + "s&7, jutalmak: &f" + ct.getRewards().size() + "&7)"));
                }
            }
            case "reload" -> {
                if (!checkAdmin(sender)) return true;
                plugin.getHologramManager().removeAll();
                plugin.getAnimationManager().stopAll();
                plugin.reloadConfig();
                plugin.getCrateManager().loadCrateTypes();
                plugin.getHologramManager().loadAll();
                plugin.getAnimationManager().loadAll();
                sender.sendMessage(plugin.msg("reload-done"));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private boolean checkAdmin(CommandSender sender) {
        if (!sender.hasPermission("mycrates.admin")) { sender.sendMessage(plugin.msg("no-permission")); return false; }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(SyntrixCrate.colorize("&6--- SyntrixCrate Parancsok ---"));
        sender.sendMessage(SyntrixCrate.colorize("&e/crates set <típus> &7- Láda elhelyezése"));
        sender.sendMessage(SyntrixCrate.colorize("&e/crates remove &7- Láda törlése"));
        sender.sendMessage(SyntrixCrate.colorize("&e/crates give <játékos> <típus> [db] &7- Kulcs adása"));
        sender.sendMessage(SyntrixCrate.colorize("&e/crates key <típus> &7- Kulcs adása magadnak"));
        sender.sendMessage(SyntrixCrate.colorize("&e/crates editor <típus> &7- In-game editor"));
        sender.sendMessage(SyntrixCrate.colorize("&e/crates list &7- Ládatípusok listája"));
        sender.sendMessage(SyntrixCrate.colorize("&e/crates reload &7- Config újratöltése"));
        sender.sendMessage(SyntrixCrate.colorize("&7Tipp: Shift+Jobb klikk a ládán = gyors editor"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("mycrates.admin")) return completions;
        if (args.length == 1) completions.addAll(Arrays.asList("set","remove","give","key","editor","list","reload","help"));
        else if (args.length == 2) {
            if (List.of("set","key","editor").contains(args[0].toLowerCase()))
                completions.addAll(plugin.getCrateManager().getAllCrateTypes().keySet());
            else if (args[0].equalsIgnoreCase("give"))
                plugin.getServer().getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give"))
            completions.addAll(plugin.getCrateManager().getAllCrateTypes().keySet());
        else if (args.length == 4 && args[0].equalsIgnoreCase("give"))
            completions.addAll(Arrays.asList("1","5","10","64"));
        String lower = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(lower));
        return completions;
    }
}
