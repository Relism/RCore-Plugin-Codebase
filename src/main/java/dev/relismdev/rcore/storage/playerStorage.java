package dev.relismdev.rcore.storage;

import org.bukkit.entity.Player;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class playerStorage {

    private ExecutorService executor = Executors.newFixedThreadPool(10);
    public localStorage ls = new localStorage();

    public void set(Player player, String entry, String value){
        ls.set("player", player.getUniqueId().toString(), entry, value);
    }
    public JSONObject get(Player player, String entry) {
        JSONObject result;
        result = ls.get("player", player.getUniqueId().toString(), entry);
        return result;
    }
    public JSONObject fetch(Player player){
        return ls.fetch("player", player.getUniqueId().toString());
    }
}