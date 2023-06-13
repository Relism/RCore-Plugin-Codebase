package dev.relismdev.rcore.api;

import dev.relismdev.rcore.utils.msg;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

public class SocketHandler {

    public static Socket socket;
    public static String newssid = null;
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

    public String connect(JSONObject authdata, Plugin plugin) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // initialize the latch with a count of 1
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                msg.log("Connected to server");
                socket.emit("authenticate", authdata);
            }
        });

        socket.on("authenticated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                msg.log("Authentication successful");
            }
        });

        socket.on("authentication_failed", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                msg.log("Authentication failed");
            }
        });

        socket.on("disconnect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                msg.log("Disconnected from server");
            }
        });

        socket.on("ssid", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    newssid = args[0].toString();
                    File configFile = new File(plugin.getDataFolder(), "config.yml");
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                    config.set("ssid", args[0].toString());
                    config.save(configFile);
                    plugin.reloadConfig();
                    latch.countDown(); // decrement the latch count to unblock the main thread
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        socket.connect();

        // block the main thread until the latch count reaches 0
        latch.await();

        return newssid;
    }
}