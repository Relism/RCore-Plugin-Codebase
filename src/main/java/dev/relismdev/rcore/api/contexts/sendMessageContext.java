package dev.relismdev.rcore.api.contexts;

import com.sun.net.httpserver.HttpExchange;
import dev.relismdev.rcore.api.Context;
import dev.relismdev.rcore.api.appApi;

import java.io.IOException;
import java.util.Map;

public class sendMessageContext implements Context {

    @Override
    public void perform(HttpExchange exchange, Map<String, Object> params, appApi api) throws IOException {
        api.replyOK(exchange, "message_sent");
    }
}
