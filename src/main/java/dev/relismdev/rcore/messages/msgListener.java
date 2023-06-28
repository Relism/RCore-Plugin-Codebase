package dev.relismdev.rcore.messages;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.socket.client.Socket;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.json.simple.JSONObject;

import dev.relismdev.rcore.api.socketHandler;

public class msgListener implements Listener {

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Socket socket = socketHandler.socket;
        // Create a new JSONObject to hold player information
        JSONObject playerInfo = new JSONObject();

        // Add the player's name to the playerInfo object
        playerInfo.put("name", event.getPlayer().getName());
        playerInfo.put("uuid", event.getPlayer().getUniqueId());

        // Add the player's world name to the playerInfo object
        playerInfo.put("world", event.getPlayer().getWorld().getName());

        // Add the player's coordinates to the playerInfo object
        JSONObject coordinates = new JSONObject();
        coordinates.put("x", Math.round(event.getPlayer().getLocation().getX()));
        coordinates.put("y", Math.round(event.getPlayer().getLocation().getY()));
        coordinates.put("z", Math.round(event.getPlayer().getLocation().getZ()));
        playerInfo.put("coordinates", coordinates);

        // Create a new JSONObject to hold both the player and content information
        JSONObject data = new JSONObject();

        // Add the player object and content object to the data object
        data.put("player", playerInfo);
        data.put("message", event.getMessage());

        executor.execute(() -> {
            socket.emit("forward", "rcore-ds", "discord-incoming", data);
        });
    }
}