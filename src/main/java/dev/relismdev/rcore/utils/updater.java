package dev.relismdev.rcore.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import dev.relismdev.rcore.api.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class updater {

    private final Plugin plugin;
    private final OkHttpClient client;
    public dataHandler dh = new dataHandler();

    public updater(Plugin plugin) {
        this.plugin = plugin;
        this.client = new OkHttpClient();
    }

    public boolean update(String ver) {
        boolean result = false;
        try {
            Float version = Float.valueOf(ver);
            Float latestVersion = Float.valueOf((String) fetchLatest().get("version"));
            if(latestVersion > version){
                // Download the new plugin JAR file
                msg.log("Starting to download and replace RCore v" + latestVersion + " :");
                File pluginFile = new File(plugin.getDataFolder().getParentFile(), "RCoreTemp.jar");
                URL url = new URL("https://api.relimc.com/rcore/plugin/releases/latest/download/RCore.jar");
                FileUtils.copyURLToFile(url, pluginFile);
                // Replace the old plugin JAR file with the new one
                File oldPluginFile = new File(plugin.getDataFolder().getParentFile(), plugin.getDescription().getName() + ".jar");
                if (oldPluginFile.exists()) {
                    oldPluginFile.delete();
                }
                pluginFile.renameTo(oldPluginFile);
                msg.log("Download completed.");
                // Restart the server
                msg.log("Successfully updated the plugin, " + version + " -> " + latestVersion);
                result = true;
            } else {
                msg.log("Nothing to update. Current : " + version + " Latest : " + latestVersion);
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            msg.log("Failed to update plugin: " + e.getMessage());
            result = false;
        }
        return result;
    }

    public JSONObject fetchLatest(){
        JSONObject version = null;
        try {
            version = dh.toObject(dh.reqAPI("https://api.relimc.com/rcore/plugin/releases/latest"));
        } catch (ParseException e ) { e.printStackTrace(); }
        return version;
    }

    public boolean toUpdate(String ver){
        Float version = Float.valueOf(ver);
        Float newVersion = Float.valueOf((String) fetchLatest().get("version"));
        return newVersion > version;
    }

    public void patchStatus(String ver){
        msg.log("&#a83242Checking the RCore version...");
        Float current = Float.valueOf(ver);
        Float latest = Float.valueOf((String) fetchLatest().get("version"));
        msg.log("&#a83242Current : " + current);
        msg.log("&#a83242Latest : " + latest);
        if(latest.equals(current)){
            //plugin is up to date
            msg.log("&#a83242Up to date ! Running RCore v" + current);
        } else {
            Float behind = (latest - current)*10;
            msg.log("&#a83242You are not running the latest RCore version !");
            msg.log("&#a83242You are : " + behind + " build(s) behind!");
        }
    }
}