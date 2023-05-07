package com.contribucraft.contribucraft;

import okhttp3.*;
import java.io.File;

import java.util.List;;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.ArrayList;





import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ContribuCraft extends JavaPlugin {
    @SuppressWarnings("unused")
    private String apiKey;
    @SuppressWarnings("unused")
    private String apiSecret;
    @SuppressWarnings("unused")
    private String apiUrl;

    private FileConfiguration config;
    @SuppressWarnings("unused")
    private OkHttpClient client;

    @Override
    public void onEnable() {
        config = getConfig();
        // Load configuration from config.yml
        this.saveDefaultConfig();
        this.apiKey = this.getConfig().getString("api-key");
        this.apiSecret = this.getConfig().getString("api-secret");
        this.apiUrl = this.getConfig().getString("api-url");

        // Create OkHttp client
        this.client = new OkHttpClient();

        // Register check command to run every 10 minutes
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("contribucraft.use")) {
                        String[] purchasedCommands = checkPurchasedCommands(player.getUniqueId().toString());
                        if (purchasedCommands.length > 0) {
                            executePurchasedCommands(player, purchasedCommands);
                            player.sendMessage(ChatColor.GREEN + "Purchased commands executed successfully.");
                        }
                    }
                }
            }
        }, 0L, 12000L);
    }
    private void executePurchasedCommands(CommandSender sender, String[] purchasedCommands) {
        for (String cmd : purchasedCommands) {
            Bukkit.dispatchCommand(sender, cmd);
        }
    }
    private void addPurchasedCommand(UUID player, String command) {
        List<String> purchasedCommands = config.getStringList(player.toString());
        purchasedCommands.add(command);
        config.set(player.toString(), purchasedCommands);
        saveConfig();
    }

    private String[] checkPurchasedCommands(String uuid) {
        Configuration config = getConfig();
        List<String> purchasedCommands = config.getStringList("players." + uuid + ".commands");
        return purchasedCommands.toArray(new String[0]);
    }
    private void executePurchasedCommands(String uuid) {
        Configuration config = getConfig();
        List<String> purchasedCommands = config.getStringList("players." + uuid + ".commands");
        for (String cmd : purchasedCommands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }
    public void saveConfig() {
        try {
            config.save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("redeem")) {
            if (args.length == 1) {
                String commandToRedeem = args[0];
                Configuration config = getConfig();
                if (!config.contains("purchasedCommands")) {
                    sender.sendMessage(ChatColor.RED + "There was an error while trying to redeem your command.");
                    return true;
                }
                List<String> purchasedCommands = config.getStringList("purchasedCommands");
                if (!purchasedCommands.contains(commandToRedeem)) {
                    sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
                    return true;
                }
                String[] purchasedCommand = commandToRedeem.split(" ");
                executePurchasedCommands(sender, purchasedCommand);
                String playerUUID = ((Player) sender).getUniqueId().toString();
                if (config.contains("executedCommands." + playerUUID)) {
                    List<String> executedCommands = config.getStringList("executedCommands." + playerUUID);
                    executedCommands.add(commandToRedeem);
                    config.set("executedCommands." + playerUUID, executedCommands);
                } else {
                    List<String> executedCommands = new ArrayList<>();
                    executedCommands.add(commandToRedeem);
                    config.set("executedCommands." + playerUUID, executedCommands);
                }
                saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Command redeemed successfully!");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /redeem <command>");
                return true;
            }
        }
        return false;
    }

}
