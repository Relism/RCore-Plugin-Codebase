package dev.relismdev.rcore.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

class sseHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set the content type to text/event-stream
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream");

        // Enable CORS if needed
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        // Create an output stream to send SSE events
        OutputStream os = exchange.getResponseBody();

        // Simulate sending SSE events (replace with your logic)
        for (int i = 0; i < 5; i++) {
            String event = "data: This is an SSE message " + i + "\n\n";
            os.write(event.getBytes());
            os.flush();

            // Sleep for a moment to simulate real-time updates
            try {
                Thread.sleep(1000); // 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Close the response stream when done
        os.close();
    }
}
