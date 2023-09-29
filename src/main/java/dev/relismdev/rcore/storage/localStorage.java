package dev.relismdev.rcore.storage;

import dev.relismdev.rcore.api.socketHandler;
import dev.relismdev.rcore.api.dataHandler;
import dev.relismdev.rcore.utils.msg;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class localStorage {

    public static JSONObject configData;

    private static dataHandler dh = new dataHandler();
    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public void storeConfig() throws ParseException, InterruptedException {
        JSONObject jsonData;
        Socket socket = socketHandler.socket;
        CountDownLatch latch = new CountDownLatch(1); // Create a latch with count 1
        executor.execute(() -> {
            socket.emit("storage", "request", "config");
            socket.on("storage", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        configData = (JSONObject) args[0];
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
        Socket socket = socketHandler.socket;
        try {
            if(configData != null){
                JSONObject data = new JSONObject();
                data.put(entry, value);

                if (!configData.has(field)) {
                    configData.put(field, new JSONObject());
                }
                JSONObject fielded = configData.getJSONObject(field);

                if (!fielded.has(identifier)) {
                    fielded.put(identifier, new JSONObject());
                }
                JSONObject identified = fielded.getJSONObject(identifier);

                if (!identified.has(entry) || !identified.get(entry).equals(value)) {
                    executor.execute(() -> {
                        identified.put(entry, value);
                        socket.emit("storage", "set", field, identifier, data);
                    });
                }
            }
        } catch (Exception e) {
            msg.log("&cError: " + e.getMessage());
        }
    }


    public JSONObject get(String field, String identifier, String entry) {
        JSONObject result = null;
        try {
            JSONObject fieldData = configData.getJSONObject(field);
            JSONObject identifiedData = (JSONObject) fieldData.get(identifier);
            if(identifiedData != null){
                result = new JSONObject();
                result.put("entry", entry);
                result.put("value", identifiedData.get(entry));
            } else {
                result.put("entry", "null");
                result.put("value", "null");
            }
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

    public boolean forward(String endpoint, String request, JSONObject data) {
        Socket socket = socketHandler.socket;
        if (socket != null) {
            try {
                socket.emit("forward", endpoint, request, data); // Pass the messages as arguments to emit
                return true; // Return true if the emit was successful
            } catch (Exception e) {
                e.printStackTrace(); // Handle any exceptions here
            }
        }
        return false; // Return false if the socket is not initialized or if emit fails
    }

}
