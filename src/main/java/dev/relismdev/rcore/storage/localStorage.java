package dev.relismdev.rcore.storage;

import dev.relismdev.rcore.api.SocketHandler;
import dev.relismdev.rcore.api.dataHandler;
import dev.relismdev.rcore.utils.msg;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class localStorage {

    public static JSONObject configData;

    private static dataHandler dh = new dataHandler();
    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public void storeConfig() throws ParseException, InterruptedException {
        JSONObject jsonData;
        Socket socket = SocketHandler.socket;
        CountDownLatch latch = new CountDownLatch(1); // Create a latch with count 1
        executor.execute(() -> {
            socket.emit("storage", "request", "config");
            socket.on("storage", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        configData = (JSONObject) args[1];
                        latch.countDown(); // Count down the latch when the message arrives
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        latch.await(); // Wait for the latch to count down to 0
    }

    public void set(String field, String identifier, String entry, String value) {
        Socket socket = SocketHandler.socket;
        try {
            JSONObject data = new JSONObject();
            data.put(entry, value);
            msg.log(data.toString());
            executor.execute(() -> {
                configData.getJSONObject(field).getJSONObject(identifier).put(entry, value);
                socket.emit("storage", "set", field, identifier, data);
            });
        } catch (Exception e) {
            msg.log("&cError: " + e.getMessage());
        }
    }

    public JSONObject get(String field, String identifier, String entry) {
        try {
            JSONObject fieldData = configData.getJSONObject(field);
            JSONObject identifiedData = (JSONObject) fieldData.get(identifier);
            JSONObject result = new JSONObject();
            result.put("entry", entry);
            result.put("value", identifiedData.get(entry));
            return result;
        } catch (Exception e) {
            msg.log("&cError: " + e.getMessage());
            return null;
        }
    }

    public String getSimple(String field, String entry) {
        try {
            JSONObject fieldData = configData.getJSONObject(field);
            String result = fieldData.get(entry).toString();
            return result;
        } catch (Exception e) {
            msg.log("&cError: " + e.getMessage());
            return null;
        }
    }

    public JSONObject fetch(String field, String identifier){
        JSONObject fieldData = configData.getJSONObject(field);
        JSONObject identifiedData = (JSONObject) fieldData.getJSONObject(identifier);
        return identifiedData;
    }

}
