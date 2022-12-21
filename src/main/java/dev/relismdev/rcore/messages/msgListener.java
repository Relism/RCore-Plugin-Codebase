package dev.relismdev.rcore.messages;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import okhttp3.OkHttpClient;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import dev.relismdev.rcore.api.*;
import dev.relismdev.rcore.utils.msg;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class msgListener implements Listener {

    private final Plugin plugin;
    public msgListener(Plugin plugin) {
        this.plugin = plugin;
    }
    private ExecutorService executor = Executors.newFixedThreadPool(10);
    dataHandler dh = new dataHandler();
    msgExchanger msx = new msgExchanger();

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        executor.execute(() -> {
            msx.pushMessage(event);
            try {
                msg.log((String) translateMessage(URLEncoder.encode(event.getMessage(), StandardCharsets.UTF_8), "en-US").get("text"));
            } catch (ParseException e){
                e.printStackTrace();
            }
        });
    }

    public JSONObject translateMessage(String message, String language) throws ParseException {
        String authtoken = plugin.getConfig().getString("authtoken");
        return(dh.toObject(dh.reqAPI("https://api.relimc.com/rcore/translate/?authtoken=" + authtoken + "&text=" + message + "&targetLanguage=" + language)));
    }
}
