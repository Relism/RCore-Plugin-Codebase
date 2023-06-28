package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.api.*;
import dev.relismdev.rcore.storage.localStorage;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class initializer {

    public reloader rl = new reloader();
    public localStorage ls = new localStorage();
    public appApi api = new appApi();
    public static boolean state = false;
    public static dataHandler dh = new dataHandler();
    public static misc misc = new misc();

    private static Plugin plugin;

    public initializer(Plugin pl){
        plugin = pl;
    }

    public boolean initialize(File webFolder, String apisecret){
        msg.log("Initializing the plugin...");

        socketHandler sh = new socketHandler();
        String newssid = null;
        try {
            newssid = sh.connect(plugin);
            dh.setSSID(newssid);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rl.reload();

        String ip = ls.getSimple("server", "ip");
        int port = Integer.parseInt(ls.getSimple("server", "port"));

        state = true;
        msg.log("");
        msg.log(misc.separator("&#f54254", "INTERNAL API"));
        msg.log("Initializing the Internal API...");
        api.startHttpServer(port, newssid, webFolder, apisecret);
        msg.log("Started the Internal API on ==> : &bhttp://" + ls.getSimple("server", "ip") + ":" + port);
        msg.log("");

        return state;
    }
}
