package com.syntrixcrate.animation;

import com.syntrixcrate.SyntrixCrate;
import com.syntrixcrate.model.CrateType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class AnimationManager {

    private final SyntrixCrate plugin;
    private final Map<String, BukkitTask> runningTasks = new HashMap<>();

    public AnimationManager(SyntrixCrate plugin) {
        this.plugin = plugin;
    }

    public void startAnimation(Location loc, CrateType crate) {
        if (crate.getAnimationType().equals("NONE")) return;
        String key = locKey(loc);
        stopAnimation(key);

        Particle particle = parseParticle(crate.getAnimParticle());
        Location center   = loc.clone().add(0.5, 0.5, 0.5);

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (center.getWorld() == null) return;
            double radius = 0.8;
            long tick = System.currentTimeMillis() / 50;
            for (int i = 0; i < 8; i++) {
                double angle = Math.toRadians((tick * 5 + i * 45) % 360);
                double x = center.getX() + radius * Math.cos(angle);
                double z = center.getZ() + radius * Math.sin(angle);
                double y = center.getY() + 0.3 * Math.sin(Math.toRadians(tick * 3 + i * 45));
                center.getWorld().spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
            }
        }, 0L, 2L);

        runningTasks.put(key, task);
    }

    public void stopAnimation(Location loc) { stopAnimation(locKey(loc)); }

    private void stopAnimation(String key) {
        BukkitTask task = runningTasks.remove(key);
        if (task != null) task.cancel();
    }

    public void stopAll() {
        runningTasks.values().forEach(BukkitTask::cancel);
        runningTasks.clear();
    }

    public void loadAll() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
            plugin.getCrateManager().getAllCrateTypes().forEach((id, crate) ->
                plugin.getCrateManager().getLocationsForCrate(id).forEach((key, loc) ->
                    startAnimation(loc, crate))), 45L);
    }

    public void restartAnimation(Location loc, CrateType crate) {
        stopAnimation(loc);
        startAnimation(loc, crate);
    }

    public void restartAnimationForCrate(String crateId) {
        CrateType crate = plugin.getCrateManager().getCrateType(crateId);
        if (crate == null) return;
        plugin.getCrateManager().getLocationsForCrate(crateId).forEach((key, loc) ->
            restartAnimation(loc, crate));
    }

    private Particle parseParticle(String name) {
        try { return Particle.valueOf(name.toUpperCase()); }
        catch (Exception e) { return Particle.VILLAGER_HAPPY; }
    }

    private String locKey(Location loc) {
        return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }
}
