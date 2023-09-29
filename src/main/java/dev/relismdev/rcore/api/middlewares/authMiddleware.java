package dev.relismdev.rcore.api.middlewares;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import dev.relismdev.rcore.api.appApi;
import dev.relismdev.rcore.utils.msg;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class authMiddleware {
    private final String apisecret;
    private final appApi api = new appApi();

    public authMiddleware(String apiSecret) {
        this.apisecret = apiSecret;
    }

    public boolean handle(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        String authorizationHeader = headers.getFirst("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring("Bearer ".length());

            // You should implement a secure way to compare the token (e.g., hash comparison)
            if (Objects.equals(token, apisecret)) {
                return true;
            }
        }

        // Authentication failed; send a 401 Unauthorized response
        String response = "Unauthorized";
        api.replyERROR(exchange, response);
        return false;
    }
}
