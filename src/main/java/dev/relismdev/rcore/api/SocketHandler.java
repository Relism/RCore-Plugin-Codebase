package dev.relismdev.rcore.api;

import dev.relismdev.rcore.utils.msg;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

public class SocketHandler {

    public static Socket socket;
    public static String newssid = null;
    public static Plugin plugin;
    CountDownLatch latch = new CountDownLatch(1);
    //private final Map<String, Map<String, List<Consumer<JSONObject>>>> listeners = new HashMap<>();

    public SocketHandler() {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.reconnectionDelay = 1000;
            options.reconnectionAttempts = Integer.MAX_VALUE;
            socket = IO.socket("https://server.relism.repl.co", options);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void onConnect() {
        JSONObject authdata = new JSONObject();
        authdata.put("authtoken", plugin.getConfig().getString("authtoken"));
        authdata.put("ssid", plugin.getConfig().getString("ssid"));
        msg.log("Connected to server");
        socket.emit("authenticate", authdata);
    }

    private void onAuthenticated() {
        msg.log("Authentication successful");
    }

    private void onAuthenticationFailed() {
        msg.log("Authentication failed");
    }

    private void onDisconnect() {
        msg.log("Disconnected from server");
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