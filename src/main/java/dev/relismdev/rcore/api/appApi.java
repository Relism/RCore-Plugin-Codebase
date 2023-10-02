package dev.relismdev.rcore.api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;

import dev.relismdev.rcore.api.contexts.*;
import dev.relismdev.rcore.api.contexts.sseContext;
import dev.relismdev.rcore.api.middlewares.authMiddleware;
import dev.relismdev.rcore.utils.*;

public class appApi {

    public fileHandler fh = new fileHandler();

    private final rootContext rootContext = new rootContext(this);


    public void startHttpServer(Integer port, File web, String apisecret) {
        if (!web.exists()) {
            web.mkdir();
        }
        File htmlFile = new File(web, "index.html");
        try {
            if (htmlFile.createNewFile()) {
                //file didn't exist, and has now been created
                URL url = new URL("https://raw.githubusercontent.com/Relism/RCore-API/main/index.html");
                //download a fresh file
                fh.downloadFile(url, htmlFile);
            }
        } catch (IOException e) {
            msg.log("&cError creating files in web folder: " + e.getMessage());
        }
        try {
            authMiddleware amw = new authMiddleware(apisecret);
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            contextManager ctxManager = new contextManager(server, amw);

            //static non-standardized contexts
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    rootContext.perform(exchange, web);
                }
            }).getFilters().add(new parameterFilter());

            server.createContext("/api", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if(amw.handle(exchange)){
                        replyOK(exchange, "Authenticated");
                    }
                }
            }).getFilters().add(new parameterFilter());

            //standardized dynamic contexts
            ctxManager.createContext("/api/getServerInfo", new getServerInfoContext(), true);
            ctxManager.createContext("/api/events", new sseContext(), true);

            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void replyOK(HttpExchange HttpEx, String response) {
        try {
            HttpEx.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            Headers responseHeaders = HttpEx.getResponseHeaders();
            responseHeaders.add("Content-Type", ("application/json"));
            HttpEx.sendResponseHeaders(200, response.length());
            try (OutputStream os = HttpEx.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void replyERROR(HttpExchange HttpEx, String response) {
        try {
            HttpEx.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            Headers responseHeaders = HttpEx.getResponseHeaders();
            responseHeaders.add("Content-Type", ("application/json"));
            HttpEx.sendResponseHeaders(404, response.length());
            try (OutputStream os = HttpEx.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}