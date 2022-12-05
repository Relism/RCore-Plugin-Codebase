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

        msg.log("&aInitializing the plugin...");
        String ssid = dh.pushConfigData(authtoken, port);;
        String ip = dh.configString("ip");
        msg.log("&bYour &dSession ID &bfor this session is : &d" + ssid + " &b(make sure you dont share this code)");
        msg.log("───────────────────────────────────────────────────");
        msg.log("&eSending initialization data to the API &b==>");
        //initial reload
        if(!rl.reload()){
            state = false;
        } else {
            state = true;
        }
        msg.log("&bInitializing the Internal API...");
        api.startHttpServer(authtoken, port, ssid, webFolder, apisecret);
        msg.log("&bStarted the Internal API on ==> : &dhttp://" + dh.configString("ip") + ":" + port);
        msg.log("───────────────────────────────────────────────────");

        return state;
    }
}
