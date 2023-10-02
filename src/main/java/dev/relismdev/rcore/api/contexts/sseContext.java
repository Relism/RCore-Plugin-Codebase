package dev.relismdev.rcore.api.contexts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class sseContext {

    private static List<OutputStream> clients = new ArrayList<>();

    public void perform(HttpExchange exchange) throws IOException {
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add("Content-Type", "text/event-stream");
        responseHeaders.add("Connection", "keep-alive");
        responseHeaders.add("X-Powered-By", "Native Application Server");

        exchange.sendResponseHeaders(200, 0);
        OutputStream writer = exchange.getResponseBody();

        // Add the client's output stream to the list
        clients.add(writer);
    }

    public static void sendEvent(String event, JSONObject data) {
        Iterator<OutputStream> iterator = clients.iterator();
        while (iterator.hasNext()) {
            OutputStream client = iterator.next();
            try {
                if (isOutputStreamValid(client)) {
                    sendEvent(client, event, data);
                } else {
                    iterator.remove(); // Remove the disconnected client from the list
                }
            } catch (IOException e) {
                // Handle the broken pipe error (client disconnect)
                iterator.remove(); // Remove the disconnected client from the list
                e.printStackTrace();
            }
        }
    }

    private static void sendEvent(OutputStream writer, String event, JSONObject data) throws IOException {
        String eventData = new JSONObject().put("event", event).put("data", data.toString()).toString() + "\n";
        writer.write(eventData.getBytes());
        writer.flush();
    }

    private static boolean isOutputStreamValid(OutputStream outputStream) {
        try {
            outputStream.write(new JSONObject().toString().getBytes());
            outputStream.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void startKeepAliveThread() {
        Thread keepAliveThread = new Thread(() -> {
            while (true) {
                try {
                    sendEvent("KeepAlive", new JSONObject());
                    Thread.sleep(1000); // Send keep-alive event every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        keepAliveThread.start();
    }
}
