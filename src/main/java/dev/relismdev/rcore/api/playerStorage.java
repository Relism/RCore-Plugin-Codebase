package dev.relismdev.rcore.api;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class playerStorage {

    public SocketHandler sh = new SocketHandler();
    public static Socket socket = SocketHandler.socket;
    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public void set(Player player, String entry, String value){
        JSONObject data = new JSONObject();
        data.put(entry, value);
        executor.execute(() -> {
            socket.emit("data", "set", "player", player.getUniqueId(), data);
        });
    }
    public JSONObject get(Player player, String entry){
        JSONObject data = new JSONObject();
        executor.execute(() -> {
            socket.emit("data", "request", "player", player.getUniqueId(), entry);
            socket.on("data", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        data.put("subSet", args[0].toString());
                        data.put("entry", args[1].toString());
                        data.put("value", args[2].toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        return data;
    }
}
