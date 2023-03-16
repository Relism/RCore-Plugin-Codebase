package dev.relismdev.rcore.messages;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import dev.relismdev.rcore.api.dataHandler;
import io.socket.client.Socket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.json.simple.parser.ParseException;

import dev.relismdev.rcore.api.SocketHandler;

public class msgListener implements Listener {

    private final Plugin plugin;
    private static Socket socket = SocketHandler.socket;

    public msgListener(Plugin plugin, SocketHandler sh) {
        this.plugin = plugin;
    }

    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private dataHandler dh = new dataHandler();

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        String senderLanguage = event.getPlayer().getMetadata("language").get(0).asString();
        String message = event.getMessage();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String receiverLanguage = p.getMetadata("language").get(0).asString();
            if (!receiverLanguage.equals(senderLanguage)) {
                event.setCancelled(true);
                executor.execute(() -> {
                    try {
                        String translatedMessage = translateMessage(message, receiverLanguage);
                        p.sendMessage(translatedMessage);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private Handler consoleListener = new Handler() {
        @Override
        public void publish(LogRecord record) {
            String message = record.getMessage();
            String level = record.getLevel().toString();
            String formatted = level + " " + message;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                socket.emit("forward", "endpoint1", "ciao");
            });
        }

        @Override
        public void flush() {
            // No implementation needed
        }

        @Override
        public void close() throws SecurityException {
            // No implementation needed
        }
    };

    public String translateMessage(String message, String language) throws ParseException {
        String authtoken = plugin.getConfig().getString("authtoken");
        return((String) dh.toObject(dh.reqAPI("https://api.relimc.com/rcore/translate/?authtoken=" + authtoken + "&text=" + message + "&targetLanguage=" + language)).get("text"));
    }

}