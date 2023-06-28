package dev.relismdev.rcore.messages;
import dev.relismdev.rcore.api.socketHandler;
import dev.relismdev.rcore.storage.localStorage;
import dev.relismdev.rcore.utils.msg;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class msgBuilder {

    private static localStorage ls = new localStorage();

    public msgBuilder(){
        Socket socket = socketHandler.socket;
        socket.on("discord-outcoming", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                JSONObject messageData = data.getJSONObject("response");
                try {
                    String message = discord(messageData.get("authorName").toString(), messageData.get("cleanContent").toString());
                    msg.log(message);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        msg.send(player, message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String discord(String sender, String message){
        //String syntax = "&f[&bDiscord&f] {sender} &9> &f{message}";//dh.configString("dstomcsyntax");
        String syntax = ls.getSimple("discord", "client-syntax");
        syntax = syntax.replace("{sender}", sender);
        syntax = syntax.replace("{message}", message);
        return syntax;
    }

}
