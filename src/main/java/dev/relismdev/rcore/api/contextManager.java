package dev.relismdev.rcore.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import dev.relismdev.rcore.api.middlewares.authMiddleware;

import java.util.HashMap;
import java.util.Map;

public class contextManager {
    private final HttpServer server;
    private final Map<String, ContextInfo> routes = new HashMap<>();
    private final authMiddleware authMiddleware;
    private final appApi api = new appApi();

    public contextManager(HttpServer server, authMiddleware authMiddleware) {
        this.server = server;
        this.authMiddleware = authMiddleware;
    }

    public void createContext(String path, Context context, boolean authRequired) {
        HttpHandler handler = exchange -> {
            if (authRequired && !authMiddleware.handle(exchange)) {
                return;
            }
            context.perform(exchange, getParams(exchange), api);
        };
        server.createContext(path, handler);
    }

    private Map<String, Object> getParams(HttpExchange exchange) {
        Map<String, Object> params = (Map<String, Object>) exchange.getAttribute("parameters");
        return params;
    }

    private static class ContextInfo {
        private final Context context;
        private final boolean authRequired;

        public ContextInfo(Context context, boolean authRequired) {
            this.context = context;
            this.authRequired = authRequired;
        }
    }
}
