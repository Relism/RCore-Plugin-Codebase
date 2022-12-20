package dev.relismdev.rcore.messages;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import dev.relismdev.rcore.api.*;
import dev.relismdev.rcore.utils.msg;

public class msgListener implements Listener {
    private ExecutorService executor = Executors.newFixedThreadPool(10);
    dataHandler dh = new dataHandler();
    msgExchanger msx = new msgExchanger();

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        executor.execute(() -> {
            msx.pushMessage(event);
            msg.log(translateMessage(URLEncoder.encode(event.getMessage(), StandardCharsets.UTF_8), "en-US"));
        });
    }

    public String translateMessage(String message, String language){
        return(dh.reqAPI("https://api.relimc.com/rcore/translate/?authtoken" + dh.fetchToken() + "&text=" + message + "&targetLanguage=" + language));
    }
}
