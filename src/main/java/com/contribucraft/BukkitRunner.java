package com.contribucraft;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitRunner extends BukkitRunnable {
    public final ContribuCraft plugin;

    BukkitRunner(ContribuCraft plugin) {
        this.plugin = plugin;
    }

    public void run() {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)ContribuCraft.instance, () -> {
            try {
                this.plugin.check();
            } catch (Exception e) {
                this.plugin.getLogger().warning(e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
