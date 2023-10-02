package dev.relismdev.rcore.api.contexts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import dev.relismdev.rcore.api.Context;
import dev.relismdev.rcore.api.appApi;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class sseContext implements Context {

    private static List<OutputStream> clients = new ArrayList<>();

    @Override
    public void perform(HttpExchange exchange, Map<String, Object> params, appApi api) throws IOException {
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

}
