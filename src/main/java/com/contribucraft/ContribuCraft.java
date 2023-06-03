package com.contribucraft;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.contribucraft.Woo.Order;
import com.contribucraft.Woo.WMCWoo;
import com.contribucraft.Woo.WMCProcessedOrders;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import org.bukkit.plugin.java.JavaPlugin;

public final class ContribuCraft extends JavaPlugin {
    static ContribuCraft instance;

    private YamlConfiguration l10n;

    public static final String NL = System.getProperty("line.separator");

    private List<String> PlayersMap = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        if (
                !Bukkit.getOnlineMode() &&
                        !Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")
        ) {
            getLogger().severe(String.valueOf(Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")));
            getLogger().severe("ContribuCraft doesn't support offLine mode");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        YamlConfiguration config = (YamlConfiguration) getConfig();
        // Save the default config.yml
        try{
            saveDefaultConfig();
        } catch ( IllegalArgumentException e ) {
            getLogger().warning( e.getMessage() );
        }

        String lang = getConfig().getString("lang");
        if ( lang == null ) {
            getLogger().warning( "No default l10n set, setting to english." );
        }

        // Load the commands.
        getCommand( "contribucraft" ).setExecutor( new WooCommand() );

        // Log when plugin is initialized.
        getLogger().info( this.getLang( "log.com_init" ));

        // Setup the scheduler
        BukkitRunner scheduler = new BukkitRunner(instance);
        scheduler.runTaskTimerAsynchronously( instance, config.getInt( "update_interval" ) * 20, config.getInt( "update_interval" ) * 20 );

        // Log when plugin is fully enabled ( setup complete ).
        getLogger().info( this.getLang( "log.enabled" ) );
    }


    public void onDisable() {
        getLogger().info(getLang("log.com_init"));
    }

    String getLang(String path) {
        if (null == this.l10n) {
            LangSetup lang = new LangSetup(instance);
            this.l10n = lang.loadConfig();
        }
        return this.l10n.getString(path);
    }

    private void validateConfig() throws Exception {
        if (1 > getConfig().getString("url").length())
            throw new Exception("Server URL is empty, check config.");
        if (getConfig().getString("url").equals("http://playground.dev"))
            throw new Exception("URL is still the default URL, check config.");
        if (1 > getConfig().getString("key").length())
            throw new Exception("Server Key is empty, this is insecure, check config.");
    }

    public URL getSiteURL() throws Exception {
        boolean usePrettyPermalinks = getConfig().getBoolean("prettyPermalinks");
        String baseUrl = getConfig().getString("url") + "/wp-json/wmc/v1/server/";
        if (!usePrettyPermalinks) {
            baseUrl = getConfig().getString("url") + "/index.php?rest_route=/wmc/v1/server/";
            String customRestUrl = getConfig().getString("restBasePath");
            if (!customRestUrl.isEmpty())
                baseUrl = customRestUrl;
        }
        debug_log("Checking base URL: " + baseUrl);
        return new URL(baseUrl + getConfig().getString("key"));
    }

    boolean check() throws Exception {
        validateConfig();
        String pendingOrders = getPendingOrders();
        debug_log("Logging website reply" + NL + pendingOrders.substring(0, Math.min(pendingOrders.length(), 64)) + "...");
        if (pendingOrders.isEmpty()) {
            debug_log("Pending orders is empty completely", Integer.valueOf(2));
            return false;
        }
        Gson gson = (new GsonBuilder()).create();
        WMCWoo WMCWoo = (WMCWoo)gson.fromJson(pendingOrders, WMCWoo.class);
        List<Order> orderList = WMCWoo.getOrders();
        if (WMCWoo.getData() != null) {
            wmc_log("Code:" + WMCWoo.getCode(), Integer.valueOf(3));
            throw new Exception(WMCWoo.getMessage());
        }
        if (orderList == null || orderList.isEmpty()) {
            wmc_log("No orders to process.", Integer.valueOf(2));
            return false;
        }
        List<Integer> processedOrders = new ArrayList<>();
        for (Order order : orderList) {
            Player player = getServer().getPlayerExact(order.getPlayer());
            if (null == player) {
                debug_log("Player was null for an order", Integer.valueOf(2));
                continue;
            }
            if (getConfig().isSet("whitelist-worlds")) {
                List<String> whitelistWorlds = getConfig().getStringList("whitelist-worlds");
                String playerWorld = player.getWorld().getName();
                if (!whitelistWorlds.contains(playerWorld)) {
                    wmc_log("Player " + player.getDisplayName() + " was in world " + playerWorld + " which is not in the white-list, no commands were ran.");
                    continue;
                }
            }
            for (String command : order.getCommands()) {
                if (!isPaidUser(player)) {
                    debug_log("User is not a paid player " + player.getDisplayName());
                    return false;
                }
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                scheduler.scheduleSyncDelayedTask((Plugin)instance, () -> Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getServer().getConsoleSender(), command), 20L);
            }
            debug_log("Adding item to list - " + order.getOrderId());
            processedOrders.add(order.getOrderId());
            debug_log("Processed length is " + processedOrders.size());
        }
        if (processedOrders.isEmpty())
            return false;
        return sendProcessedOrders(processedOrders);
    }

    private boolean sendProcessedOrders(List<Integer> processedOrders) throws Exception {
        Gson gson = new Gson();
        WMCProcessedOrders wmcProcessedOrders = new WMCProcessedOrders();
        wmcProcessedOrders.setProcessedOrders(processedOrders);
        String orders = gson.toJson(wmcProcessedOrders);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), orders);
        Request request = (new Request.Builder()).url(getSiteURL()).post(body).build();
        Response response = client.newCall(request).execute();
        if (null == response.body())
            throw new Exception("Received empty response from your server, check connections.");
        WMCWoo WMCWoo = (WMCWoo)gson.fromJson(response.body().string(), WMCWoo.class);
        if (null != WMCWoo.getCode()) {
            wmc_log("Received error when trying to send post data:" + WMCWoo.getCode(), Integer.valueOf(3));
            throw new Exception(WMCWoo.getMessage());
        }
        return true;
    }

    public boolean isDebug() {
        return getConfig().getBoolean("debug");
    }

    private String getPendingOrders() throws Exception {
        URL baseURL = getSiteURL();
        BufferedReader input = null;
        try {
            Reader streamReader = new InputStreamReader(baseURL.openStream());
            input = new BufferedReader(streamReader);
        } catch (IOException e) {
            String key = getConfig().getString("key");
            String msg = e.getMessage();
            if (msg.contains(key))
                msg = msg.replace(key, "******");
            wmc_log(msg);
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null)
            buffer.append(line);
        input.close();
        return buffer.toString();
    }

    private void wmc_log(String message) {
        wmc_log(message, Integer.valueOf(1));
    }

    private void debug_log(String message) {
        if (isDebug())
            wmc_log(message, Integer.valueOf(1));
    }

    private void debug_log(String message, Integer level) {
        if (isDebug())
            wmc_log(message, level);
    }

    private void wmc_log(String message, Integer level) {
        if (!isDebug())
            return;
        switch (level.intValue()) {
            case 1:
                getLogger().info(message);
                break;
            case 2:
                getLogger().warning(message);
                break;
            case 3:
                getLogger().severe(message);
                break;
        }
    }

    private boolean isPaidUser(Player player) {
        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString().replace("-", "");
        String playerKeyBase = playerName + ':' + playerUUID + ':';
        String validPlayerKey = playerKeyBase + '\001';
        String invalidPlayerKey = playerKeyBase + Character.MIN_VALUE;
        if (Bukkit.getServer().getOnlineMode()) {
            wmc_log("Server is in online mode.", Integer.valueOf(3));
            return true;
        }
        if (!Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")) {
            wmc_log("Server in offline Mode", Integer.valueOf(3));
            return false;
        }
        if (this.PlayersMap.toString().contains(playerKeyBase)) {
            boolean valid = this.PlayersMap.contains(validPlayerKey);
            if (!valid) {
                player.sendMessage("Mojang Auth: Please Speak with a admin about your purchase");
                wmc_log("Offline mode not supported", Integer.valueOf(3));
            }
            return valid;
        }
        debug_log("Player was not in the key set " + NL + this.PlayersMap.toString());
        try {
            URL mojangUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            InputStream inputStream = mojangUrl.openStream();
            Scanner scanner = new Scanner(inputStream);
            String apiResponse = scanner.next();
            debug_log("Logging stream data:" + NL + inputStream

                    .toString() + NL + apiResponse + NL + playerName + NL + playerUUID);
            if (!apiResponse.contains(playerName)) {
                this.PlayersMap.add(invalidPlayerKey);
                throw new IOException("Mojang Auth: PlayerName doesn't exist");
            }
            if (!apiResponse.contains(playerUUID)) {
                this.PlayersMap.add(invalidPlayerKey);
                throw new IOException("Mojang Auth: PlayerName doesn't match uuid for account");
            }
            this.PlayersMap.add(validPlayerKey);
            debug_log(this.PlayersMap.toString());
            return true;
        } catch (MalformedURLException urlException) {
            debug_log("Malformed URL: " + urlException.getMessage(), Integer.valueOf(3));
            player.sendMessage("Mojang API Error: Please try again later or contact an admin about your purchase.");
        } catch (IOException e) {
            debug_log("Map is " + this.PlayersMap.toString());
            debug_log("Message when getting URL data " + e.getMessage(), Integer.valueOf(3));
            player.sendMessage("Mojang Auth: Please Speak with a admin about your purchase");
        }
        return false;
    }
}
