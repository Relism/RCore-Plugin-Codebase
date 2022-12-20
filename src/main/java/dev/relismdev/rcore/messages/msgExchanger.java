package dev.relismdev.rcore.messages;

import dev.relismdev.rcore.utils.msg;
import org.bukkit.event.player.PlayerChatEvent;
import dev.relismdev.rcore.api.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class msgExchanger {
    dataHandler dh = new dataHandler();
    public void broadcastMessage(String sender, String message, msgBuilder builder){
        msg.broadcast(builder.discord(sender, message));
    }

    public void pushMessage(PlayerChatEvent event){
        //String endpoint = dh.configString("endpoint");
        String name = URLEncoder.encode(event.getPlayer().getName(), StandardCharsets.UTF_8);
        String message = URLEncoder.encode(event.getMessage(), StandardCharsets.UTF_8);
        dh.reqAPI("https://api.relimc.com/rcore/messageEndpoint?authtoken=" + dh.fetchToken() + "&endpoint=default" + "&sender=" + name + "&message=" + message);
    }
}
