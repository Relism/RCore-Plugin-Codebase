package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.RCore;
import dev.relismdev.rcore.api.*;
import dev.relismdev.rcore.storage.localStorage;
import io.socket.client.Socket;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;

import java.io.File;

public class initializer {

    public reloader rl = new reloader();
    public localStorage ls = new localStorage();
    public appApi api = new appApi();
    public static boolean state = false;
    public static dataHandler dh = new dataHandler();
    public static SocketHandler sh = new SocketHandler();

    private static Plugin plugin;

    public initializer(Plugin pl){
        plugin = pl;
    }

    public boolean initialize(File webFolder, String apisecret, JSONObject authdata){
        msg.log("&#a8328cInitializing the plugin...");

        String ssid = authdata.getString("ssid");
        String newssid = null;
        try {
            newssid = sh.connect(authdata, plugin);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dh.setSSID(newssid);

        rl.reload();

        String ip = ls.getSimple("server", "ip");
        int port = Integer.parseInt(ls.getSimple("server", "port"));

        state = true;
        msg.log("&#32a846──[INTERNAL API]───────────────────────────────────");
        msg.log("&#32a846Initializing the Internal API...");
        api.startHttpServer(port, ssid, webFolder, apisecret);
        msg.log("&#32a846Started the Internal API on ==> : &dhttp://" + ls.getSimple("server", "ip") + ":" + port);
        msg.log("───────────────────────────────────────────────────");

        return state;
    }
}
