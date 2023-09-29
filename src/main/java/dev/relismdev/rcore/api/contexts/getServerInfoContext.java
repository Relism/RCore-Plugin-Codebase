package dev.relismdev.rcore.api.contexts;

import com.sun.net.httpserver.HttpExchange;
import dev.relismdev.rcore.api.appApi;
import dev.relismdev.rcore.storage.playerStorage;
import dev.relismdev.rcore.utils.msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.*;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public class getServerInfoContext {

    private appApi api;

    public getServerInfoContext(appApi api) {
        this.api = api;
    }
    playerStorage ps = new playerStorage();

    public void perform(HttpExchange exchange) throws IOException {
        Map< String, Object > params = (Map < String, Object > ) exchange.getAttribute("parameters");

        //handle the request
        // msg.log("&b==> &dReceived Internal API Request for Server Information...");
        //parse the process Uptime and put it in an obj
        JSONObject uptime = new JSONObject();
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long[] uptimeUnits = convertMilliseconds(rb.getUptime());
        //putting data in the obj
        uptime.put("weeks", uptimeUnits[0]);
        uptime.put("days", uptimeUnits[1]);
        uptime.put("hours", uptimeUnits[2]);
        uptime.put("minutes", uptimeUnits[3]);
        uptime.put("seconds", uptimeUnits[4]);

        //get the hardware info in an object
        JSONObject hardware = new JSONObject();
        //parse the memoryInfo
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        long maxMemory = heapUsage.getMax() / 1024 / 1024;
        long usedMemory = heapUsage.getUsed() / 1024 / 1024;
        long availableMemory = maxMemory - usedMemory;
        // Get the operating system's CPU usage
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Integer processors = osBean.getAvailableProcessors();
        double avgLoad = osBean.getSystemLoadAverage();
        //put hardware info in the object
        hardware.put("allocated_ram", maxMemory);
        hardware.put("used_ram", usedMemory);
        hardware.put("free_ram", availableMemory);
        hardware.put("processors", processors);
        hardware.put("cpu_load", avgLoad);

        //Get the plugin list in an array
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        JSONArray pluginsArray = new JSONArray();
        for (Plugin plugin : plugins) {
            JSONObject pluginData = new JSONObject();
            pluginData.put("name", plugin.getName());
            pluginData.put("version", plugin.getDescription().getVersion());
            pluginData.put("author", plugin.getDescription().getAuthors());
            pluginData.put("enabled", plugin.isEnabled());
            pluginsArray.put(pluginData);
        }

        //Get the online players names in an array
        Collection< ? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        JSONArray playersArray = new JSONArray();
        for (Player player : onlinePlayers) {
            JSONObject playerData = new JSONObject();
            playerData.put("username", player.getName());
            playerData.put("uuid", player.getUniqueId());
            playerData.put("data", ps.fetch(player));
            playersArray.put(playerData);
        }

        Properties serverProperties = new Properties();
        InputStream input = new FileInputStream("server.properties");
        serverProperties.load(input);
        input.close();

        JSONObject serverPropertiesData = new JSONObject();
        for (String key: serverProperties.stringPropertyNames()) {
            String value = serverProperties.getProperty(key);
            serverPropertiesData.put(key, value);
        }

        //create the JSON obj
        JSONObject obj = new JSONObject();
        obj.put("server_version", Bukkit.getBukkitVersion());
        obj.put("hardware", hardware);
        obj.put("uptime", uptime);
        obj.put("player_count", onlinePlayers.size());
        obj.put("players", playersArray);
        obj.put("plugin_count", plugins.length);
        obj.put("plugins", pluginsArray);
        obj.put("properties", serverPropertiesData);

        //return the JSON obj as response
        api.replyOK(exchange, obj.toString(2));
    }
    public static long[] convertMilliseconds(long ms) {
        // Calculate the number of weeks
        long weeks = ms / 1000 / 60 / 60 / 24 / 7;
        ms = ms - (weeks * 1000 * 60 * 60 * 24 * 7);
        // Calculate the number of days
        long days = ms / 1000 / 60 / 60 / 24;
        ms = ms - (days * 1000 * 60 * 60 * 24);
        // Calculate the number of hours
        long hours = ms / 1000 / 60 / 60;
        ms = ms - (hours * 1000 * 60 * 60);
        // Calculate the number of minutes
        long minutes = ms / 1000 / 60;
        ms = ms - (minutes * 1000 * 60);
        // Calculate the number of seconds
        long seconds = ms / 1000;
        // Return the result as an array
        return new long[] {
                weeks,
                days,
                hours,
                minutes,
                seconds
        };
    }
}
