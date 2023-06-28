package dev.relismdev.rcore.api;

import dev.relismdev.rcore.utils.misc;
import dev.relismdev.rcore.utils.msg;
import dev.relismdev.rcore.utils.nodeTester;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

public class socketHandler {

    public static Socket socket = null;
    public static String newssid = null;
    public static Plugin plugin;
    public static misc misc = new misc();
    public static nodeTester nd = new nodeTester();

    CountDownLatch latch = new CountDownLatch(1);

    public socketHandler() {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.reconnectionDelay = 1000;
            options.reconnectionAttempts = Integer.MAX_VALUE;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Socket evalSocket(){
        Socket socket = null;
        try {
            String socketUrl = nd.run(plugin.getConfig().getString("node"));
            socket = IO.socket(socketUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return socket;
    }

    private void onConnect() {
        JSONObject authdata = new JSONObject();
        authdata.put("authtoken", plugin.getConfig().getString("authtoken"));
        authdata.put("ssid", plugin.getConfig().getString("ssid"));
        msg.log("Connected to server");
        socket.emit("authenticate", authdata);
        msg.broadcast("Succesfully established a connection to the API");
    }

    private void onAuthenticated() {
        msg.log("Authentication successful");
    }

    private void onAuthenticationFailed() {
        msg.log("Authentication failed");
    }

    private void onDisconnect() {
        msg.log("Disconnected from server");
        msg.broadcast("Disconnected from the API, attempting a reconnection ASAP");
        try {
            String socketUrl = nd.run(plugin.getConfig().getString("node"));
            socket = IO.socket(socketUrl);
        } catch (URISyntaxException e){
            e.printStackTrace();
        }
    }

    private void onSsidReceived(Object... args) {
        try {
            newssid = args[0].toString();
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            config.set("ssid", args[0].toString());
            config.save(configFile);
            plugin.reloadConfig();
            latch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String connect(Plugin plugin) throws InterruptedException {
        this.plugin = plugin;

        try {
            String socketUrl = nd.run(plugin.getConfig().getString("node"));
            socket = IO.socket(socketUrl);
        } catch (URISyntaxException e){
            e.printStackTrace();
        }

        socket.on(Socket.EVENT_CONNECT, args -> onConnect());
        socket.on("authenticated", args -> onAuthenticated());
        socket.on("authentication_failed", args -> onAuthenticationFailed());
        socket.on("disconnect", args -> onDisconnect());
        socket.on("ssid", this::onSsidReceived);

        socket.connect();
        latch.await();

        return newssid;
    }
}