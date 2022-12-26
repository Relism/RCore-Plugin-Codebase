package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.api.dataHandler;
import dev.relismdev.rcore.api.*;
import java.io.File;

public class initializer {

    public dataHandler dh = new dataHandler();
    public reloader rl = new reloader();
    public appApi api = new appApi();
    public static boolean state = false;

    public boolean initialize(String authtoken, Integer port, File webFolder, String apisecret){

        msg.log("&#a8328cInitializing the plugin...");
        String ssid = dh.pushConfigData(authtoken, port);;
        String ip = dh.configString("ip");
        msg.log("&6Session ID &#a8328cfor this session is : &d" + ssid + " &#a8328c(make sure you dont share this code)");
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

    public void onStart(double ETA, String authtoken){
        dh.reqAPI("https://api.relimc.com/rcore/event/onStart/?authtoken=" + authtoken + "");
    }
}
