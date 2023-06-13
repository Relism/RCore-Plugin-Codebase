package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.RCore;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class misc {

    private RCore plugin;
    //Miscellanous stuff
    public boolean isPremium(Player player) {
        // check if the player is online
        if (player.isOnline()) {
            // get the player's name
            String playerName = player.getName();
            // send a request to the Mojang API to check if the player's name is associated with a premium account
            try {
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.connect();
                // check the response code
                if (connection.getResponseCode() == 200) {
                    // the player is premium
                    return true;
                } else {
                    // the player is not premium
                    return false;
                }
            } catch (IOException e) {
                // there was an error connecting to the API, return false
                return false;
            }
        } else {
            // the player is not online, return false
            return false;
        }
    }
    public JSONObject strToObj(String data){
        JSONObject json = null;
        try {
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(data);

            return json;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return json;
    }
}
