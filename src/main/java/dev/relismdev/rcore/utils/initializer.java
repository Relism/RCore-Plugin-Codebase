package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.api.dataHandler;
import dev.relismdev.rcore.api.*;
import io.socket.client.Socket;

import java.io.File;

public class initializer {

    public dataHandler dh = new dataHandler();
    public reloader rl = new reloader();
    public appApi api = new appApi();
    public static boolean state = false;
    public static Socket socket = SocketHandler.socket;
    public static nodePointer pointer = new nodePointer();

    public boolean initialize(int port, File webFolder, String apisecret, String ssid){

        msg.log("&#a8328cInitializing the plugin...");

        String ip = dh.configString("ip");
        msg.log("&#a8328cSending initialization data to the API &e==>");
        state = true;
        msg.log("&#32a846──[INTERNAL API]───────────────────────────────────");
        msg.log("&#32a846Initializing the Internal API...");
        api.startHttpServer(port, ssid, webFolder, apisecret);
        msg.log("&#32a846Started the Internal API on ==> : &dhttp://" + dh.configString("ip") + ":" + port);
        msg.log("───────────────────────────────────────────────────");

        return state;
    }
}
