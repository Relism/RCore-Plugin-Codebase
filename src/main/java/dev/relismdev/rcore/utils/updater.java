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
import org.json.JSONObject;
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
            String version = ver;
            String latestVersion = (String) fetchLatest().get("date");
            msg.log(latestVersion);
            if(toUpdate(ver)){
                // Download the new plugin JAR file
                msg.log("Starting to download and replace RCore v" + latestVersion + " :");
                String latestPluginName = "RCore-" + latestVersion + ".jar";
                File pluginFile = new File(plugin.getDataFolder().getParentFile(), latestPluginName);
                URL url = new URL("https://evalfolder.relism.repl.co/plugin/releases/download/" + latestVersion);
                FileUtils.copyURLToFile(url, pluginFile);
                // Replace the old plugin JAR file with the new one
                File oldPluginFile = new File(plugin.getDataFolder().getParentFile(), plugin.getDescription().getName() + ".jar");
                if (oldPluginFile.exists()) {
                    oldPluginFile.delete();
                }
                msg.log("Download completed.");
                // Restart the server
                msg.log("Successfully updated the plugin, " + version + " -> " + latestPluginName);
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
        version = dh.reqAPI("https://evalfolder.relism.repl.co/plugin/releases/latest");
        return version;
    }

    public boolean toUpdate(String ver){
        String newVersion = (String) fetchLatest().get("date");
        return !ver.equals(newVersion);
    }

    public void patchStatus(String ver){
        msg.log("&#a83242Checking the RCore version...");
        String current = ver;
        String latest = (String) fetchLatest().get("date");
        msg.log("&6Current : &d" + current);
        msg.log("&6Latest : &d" + latest);
        if(latest.equals(current)){
            //plugin is up to date
            msg.log("&#a83242Up to date ! Running RCore v" + current);
        } else {
            //Float behind = (latest - current)*10;
            msg.log("You are not running the latest RCore version !");
            //msg.log("You are : " + behind + " build(s) behind!");
        }
    }
}