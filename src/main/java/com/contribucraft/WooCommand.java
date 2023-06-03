package com.contribucraft;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
public class WooCommand implements TabExecutor {
    public ContribuCraft plugin = ContribuCraft.instance;

    private final String chatPrefix = ChatColor.translateAlternateColorCodes('&', "&5[&fWooMinecraft&5] ");

    private final HashMap<String, String> subCommands = new HashMap<>();

    public WooCommand() {
        this.subCommands.put("help", "contribucraft.admin");
        this.subCommands.put("check", "contribucraft.admin");
        this.subCommands.put("ping", "contribucraft.admin");
        this.subCommands.put("debug", "contribucraft.admin");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission("contribucraft.admin")) {
                sender.sendMessage(this.chatPrefix + this.plugin.getLang("general.avail_commands") + ": /contribucraft help");
            } else {
                sender.sendMessage(this.chatPrefix + this.plugin.getLang("general.not_authorized"));
            }
            return true;
        }
        if (args.length <= 1 && !args[0].equalsIgnoreCase("ping")) {
            if (args[0].equalsIgnoreCase("check")) {
                checkSubcommand(sender);
            } else if (args[0].equalsIgnoreCase("debug")) {
                debugSubcommand(sender);
            } else if (args[0].equalsIgnoreCase("help")) {
                helpSubcommand(sender);
            } else {
                sender.sendMessage(this.chatPrefix + "Usage: /contribucraft help");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("ping"))
            try {
                if (!args[1].isEmpty())
                    pingSubcommand(sender, args.length, args[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                pingSubcommand(sender, args.length, "");
            }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length != 1)
            return null;
        for (Map.Entry<String, String> subCommand : this.subCommands.entrySet()) {
            if (((String)subCommand.getKey()).startsWith(args[0]) && sender.hasPermission(subCommand.getValue()))
                completions.add(subCommand.getKey());
        }
        return completions;
    }

    private void checkSubcommand(CommandSender sender) {
        if (!sender.hasPermission("contribucraft.admin")) {
            String msg = this.chatPrefix + ChatColor.translateAlternateColorCodes('&', this.plugin.getLang("general.not_authorized"));
            sender.sendMessage(msg);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                String msg = this.chatPrefix + " ";
                if (this.plugin.check()) {
                    msg = msg + this.plugin.getLang("general.processed");
                } else {
                    msg = msg + this.plugin.getLang("general.none_avail");
                }
                sender.sendMessage(msg);
            } catch (Exception e) {
                if (e.getMessage().contains("Expected BEGIN_OBJECT but was STRING")) {
                    sender.sendMessage(this.chatPrefix + ChatColor.RED + "REST endpoint is not accessible, check logs.");
                    return;
                }
                sender.sendMessage(this.chatPrefix + ChatColor.RED + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void pingSubcommand(CommandSender sender, int length, String Url) {
        if (!sender.hasPermission("contribucraft.admin")) {
            String msg = this.chatPrefix + ChatColor.translateAlternateColorCodes('&', this.plugin.getLang("general.not_authorized"));
            sender.sendMessage(msg);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            String msg = "";
            if (length == 2) {
                try {
                    URL U = new URL(Url);
                    HttpURLConnection ping = (HttpURLConnection)U.openConnection();
                    ping.setConnectTimeout(1000);
                    ping.setReadTimeout(1000);
                    ping.setRequestMethod("HEAD");
                    int Rc = ping.getResponseCode();
                    String rs = ping.getResponseMessage();
                    ping.disconnect();
                    if (Rc < 199) {
                        msg = this.chatPrefix + ChatColor.YELLOW + " Status: Ok, but possible issues, " + Rc + " " + rs;
                    } else if (Rc >= 200 && Rc <= 299) {
                        msg = this.chatPrefix + ChatColor.GREEN + " Status: Good, " + Rc;
                    } else if (Rc >= 300 && Rc <= 399) {
                        msg = this.chatPrefix + ChatColor.YELLOW + " Status: Ok, but possible issues, " + Rc + " " + rs;
                    } else if (Rc >= 400 && Rc <= 599) {
                        msg = this.chatPrefix + ChatColor.DARK_RED + " Status: Bad, " + Rc + " " + rs;
                    }
                    sender.sendMessage(msg);
                } catch (IOException e) {
                    ContribuCraft.instance.getLogger().severe(e.getMessage());
                    sender.sendMessage(this.chatPrefix + ChatColor.DARK_RED + "Server Status: Failed");
                }
            } else {
                try {
                    sender.sendMessage(this.chatPrefix + "Checking connection to server");
                    HttpURLConnection ping = (HttpURLConnection)(new URL(this.plugin.getConfig().getString("url"))).openConnection();
                    ping.setConnectTimeout(700);
                    ping.setReadTimeout(700);
                    ping.setRequestMethod("HEAD");
                    int Rc = ping.getResponseCode();
                    String rs = ping.getResponseMessage();
                    ping.disconnect();
                    if (Rc < 199) {
                        msg = this.chatPrefix + ChatColor.YELLOW + " Status: Ok, but possible issues, " + Rc + " " + rs;
                    } else if (Rc >= 200 && Rc <= 299) {
                        msg = this.chatPrefix + ChatColor.GREEN + " Status: Good, " + Rc;
                    } else if (Rc >= 300 && Rc <= 399) {
                        msg = this.chatPrefix + ChatColor.YELLOW + " Status: Ok, but possible issues, " + Rc + " " + rs;
                    } else if (Rc >= 400 && Rc <= 599) {
                        msg = this.chatPrefix + ChatColor.DARK_RED + " Status: Bad, " + Rc + " " + rs;
                    }
                    sender.sendMessage(msg);
                } catch (IOException e) {
                    ContribuCraft.instance.getLogger().severe(e.getMessage());
                    sender.sendMessage(this.chatPrefix + ChatColor.DARK_RED + "Server Status: Failed");
                    if (this.plugin.isDebug()) {
                        ContribuCraft.instance.getLogger().info(this.plugin.getConfig().getString("key"));
                        ContribuCraft.instance.getLogger().info(this.plugin.getConfig().getString("url"));
                    }
                }
                try {
                    sender.sendMessage(this.chatPrefix + "Checking Rest Api Url");
                    HttpURLConnection ping = (HttpURLConnection)ContribuCraft.instance.getSiteURL().openConnection();
                    ping.setConnectTimeout(700);
                    ping.setReadTimeout(700);
                    ping.setRequestMethod("HEAD");
                    int Rc = ping.getResponseCode();
                    String rs = ping.getResponseMessage();
                    ping.disconnect();
                    if (Rc < 199) {
                        msg = this.chatPrefix + ChatColor.YELLOW + " Status: Ok, but possible issues, " + Rc + " " + rs;
                    } else if (Rc >= 200 && Rc <= 299) {
                        msg = this.chatPrefix + ChatColor.GREEN + " Status: Good, " + Rc;
                    } else if (Rc >= 300 && Rc <= 399) {
                        msg = this.chatPrefix + ChatColor.YELLOW + " Status: Ok, but possible issues, " + Rc + " " + rs;
                    } else if (Rc >= 400 && Rc <= 599) {
                        msg = this.chatPrefix + ChatColor.DARK_RED + " Status: Bad, " + Rc + " " + rs;
                    }
                    sender.sendMessage(msg);
                } catch (IOException e) {
                    ContribuCraft.instance.getLogger().severe(e.getMessage());
                    sender.sendMessage(this.chatPrefix + ChatColor.DARK_RED + "Server Status: Failed");
                    if (this.plugin.isDebug()) {
                        ContribuCraft.instance.getLogger().info(this.plugin.getConfig().getString("key"));
                        ContribuCraft.instance.getLogger().info(this.plugin.getConfig().getString("url"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void debugSubcommand(CommandSender sender) {
        FileConfiguration pluginConfig = this.plugin.getConfig();
        if (pluginConfig.getBoolean("debug")) {
            pluginConfig.set("debug", Boolean.valueOf(false));
            sender.sendMessage(this.chatPrefix + "Set debug to: " + ChatColor.DARK_RED + "False");
            return;
        }
        pluginConfig.set("debug", Boolean.valueOf(true));
        sender.sendMessage(this.chatPrefix + "Set debug to: " + ChatColor.GREEN + "True");
    }

    private void helpSubcommand(CommandSender sender) {
        PluginDescriptionFile descriptionFile = this.plugin.getDescription();
        sender.sendMessage(this.chatPrefix + " Ver" + descriptionFile.getVersion());
        sender.sendMessage(ChatColor.DARK_PURPLE + "By " + String.join(",", descriptionFile.getAuthors()));
        sender.sendMessage(ChatColor.DARK_PURPLE + "/contribucraft help" + ChatColor.WHITE + " Shows this Helpsite");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/contribucraft check" + ChatColor.WHITE + " Check for donations/orders");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/contribucraft ping" + ChatColor.WHITE + " Test server connection");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/contribucraft debug" + ChatColor.WHITE + " Enable/disable debugging");
    }
}
