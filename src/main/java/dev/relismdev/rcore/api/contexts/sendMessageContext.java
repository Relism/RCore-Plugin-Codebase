package dev.relismdev.rcore.api.contexts;

import com.sun.net.httpserver.HttpExchange;
import dev.relismdev.rcore.api.appApi;

import java.io.IOException;
import java.util.Map;

public class sendMessageContext {

    private appApi api;

    public sendMessageContext(appApi api) {
        this.api = api;
    }

    public void perform(HttpExchange exchange) throws IOException {
        Map< String, Object > params = (Map < String, Object > ) exchange.getAttribute("parameters");
        //handle the request
        //msg.log("&b==> &dReceived Internal API Request for Server Information...");
        String sender = params.get("sender").toString();
        String message = params.get("message").toString();
        api.replyOK(exchange, "message_sent");
    }
}
