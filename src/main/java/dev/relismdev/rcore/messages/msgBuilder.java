package dev.relismdev.rcore.messages;
import dev.relismdev.rcore.api.SocketHandler;
import dev.relismdev.rcore.utils.msg;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class msgBuilder {

    public msgBuilder(){
        Socket socket = SocketHandler.socket;
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
        String syntax = "&f[&bDiscord&f] {sender} &9> &f{message}";//dh.configString("dstomcsyntax");
        syntax = syntax.replace("{sender}", sender);
        syntax = syntax.replace("{message}", message);
        return syntax;
    }

}
