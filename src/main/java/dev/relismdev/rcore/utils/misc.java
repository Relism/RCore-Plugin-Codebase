package dev.relismdev.rcore.utils;

import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class misc {
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
}
