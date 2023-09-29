package dev.relismdev.rcore.storage;

import org.bukkit.entity.Player;
import org.json.JSONObject;

public class loggerStorage {

    public localStorage ls = new localStorage();

    public void set(String type, String entry, JSONObject data){
        ls.set("logger", type, entry, data.toString());
    }
    /*public String get(Player player, String entry) {
        String result = ls.get("player", player.getUniqueId().toString(), entry).get("value").toString();
        return result;
    }*/

}
