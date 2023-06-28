package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.RCore;
import dev.relismdev.rcore.api.dataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class misc {

    private static dataHandler dh = new dataHandler();

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

    public void checkSoftDep(String pluginName){
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (plugin != null) {
                msg.log("&#34deeb• &fHooked into " + plugin.getName() + " &#34deebv" + plugin.getPluginMeta().getVersion() + "&f!");
            } else {
                msg.log("&#eb4034• &fCouldnt find " + pluginName + ", continuing without it");
            }
        } catch(Error e) {
            msg.log("&#eb4034• &fCouldnt find " + pluginName + ", continuing without it");
        }
    }

    public String separator(String... args) {
        String color = "";
        String text = "";
        if (args.length > 0) {
            color = args[0] + " ";
        }
        if (args.length > 1) {
            text = args[1] + " ";
        }
        if (color.isEmpty() && text.isEmpty()) {
            return "╰─────────────────────────────────────────────────────";
        } else {
            return "&f╭──[" + color + text + "&f]──────────────────────────────────────";
        }
    }

    public boolean checkConfig(Plugin plugin) {
        Map<String, String> fields = new HashMap<>();
        fields.put("authtoken", "&cSeems like you didn't set up the &bauthtoken &cin the config.yml file! Set it up properly and &brestart the server&c.");
        fields.put("port", "&cSeems like you didn't set up a &bport &cin the &bconfig.yml &cfile! Set it up properly and &brestart the server&c.");
        String accept_terms = plugin.getConfig().getString("accept-terms");
        if (accept_terms == null || !Boolean.parseBoolean(accept_terms)) {
            msg.log("&#eb4034• &fCouldn't verify &#eb4034accept_terms");
            fields.put("accept_terms", "&cYou have to accept &bRCore's api terms &d( https://apidocs.relimc.com ) &cin order to use this plugin!\n&cTo do so, set the &b'accept-terms' field &cin the &bconfig.yml &cfile to true and &brestart the server&c.");
            msg.log(separator());
        } else {
            msg.log("&#B3FFC6• &fVerified &#B3FFC6accept_terms");
        }
        fields.put("apisecret", "&cSeems like you didn't set up an &bapisecret &cin the &bconfig.yml &cfile! Set it up properly and &brestart the server&c.");
        for (String key : fields.keySet()) {
            String value = plugin.getConfig().getString(key);
            if (value == null || value.trim().isEmpty()) {
                msg.log("&#eb4034• &fCouldn't verify &#eb4034" + key);
                msg.log(fields.get(key));
                msg.log(separator());
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return false;
            } else {
                msg.log("&#B3FFC6• &fVerified &#B3FFC6" + key);
            }
        }
        return true;
    }

    public void printAsciiArt(String color, String font, String text) {
        String url = "https://api.relimc.com/asciiart?font=" + font + "&text=" + text;
        JSONObject rawData = dh.reqAPI(url);
        JSONArray asciiArtArray = rawData.getJSONArray("asciiArt");
        String[] asciiArt = new String[asciiArtArray.length()];
        for (int i = 0; i < asciiArtArray.length(); i++) {
            asciiArt[i] = asciiArtArray.getString(i);
        }
        for (String line : asciiArt) {
            msg.log(color + line);
        }
    }
}
