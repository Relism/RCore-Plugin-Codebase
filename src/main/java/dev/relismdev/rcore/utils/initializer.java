package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.api.dataHandler;
import dev.relismdev.rcore.api.*;
import java.io.File;

public class initializer {

    public dataHandler dh = new dataHandler();
    public reloader rl = new reloader();
    public appApi api = new appApi();
    public static boolean state = false;

    public boolean initialize(String authtoken, Integer port, File webFolder, String apisecret, String ssid){

        msg.log("&#a8328cInitializing the plugin...");
        dh.pushConfigData(authtoken, port, ssid);;
        String ip = dh.configString("ip");
        msg.log("&#a8328cSending initialization data to the API &e==>");
        //initial reload
        if(!rl.reload()){
            state = false;
        } else {
            state = true;
        }
        msg.log("&#32a846──[INTERNAL API]───────────────────────────────────");
        msg.log("&#32a846Initializing the Internal API...");
        api.startHttpServer(authtoken, port, ssid, webFolder, apisecret);
        msg.log("&#32a846Started the Internal API on ==> : &dhttp://" + dh.configString("ip") + ":" + port);
        msg.log("───────────────────────────────────────────────────");

        return state;
    }
}
