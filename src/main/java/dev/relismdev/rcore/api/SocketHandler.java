package dev.relismdev.rcore.api;

import dev.relismdev.rcore.utils.msg;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;

import java.io.File;
import java.net.URISyntaxException;

public class SocketHandler {

    public static Socket socket;
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

    public void connect(JSONObject authdata, Plugin plugin) {
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
                    File configFile = new File(plugin.getDataFolder(), "config.yml");
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                    config.set("ssid", args[0].toString());
                    config.save(configFile);
                    plugin.reloadConfig();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        socket.connect();
    }

}