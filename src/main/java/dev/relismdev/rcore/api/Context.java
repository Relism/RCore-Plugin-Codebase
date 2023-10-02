package dev.relismdev.rcore.api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

public interface Context {
    void perform(HttpExchange exchange, Map< String, Object > params, appApi api) throws IOException;
}
