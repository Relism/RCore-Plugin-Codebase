package dev.relismdev.rcoresync.api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.relismdev.rcoresync.utils.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import dev.relismdev.rcoresync.utils.reloader;

public class appApi {

    public reloader rl = new reloader();
    public fileHandler fh = new fileHandler();

    public void startHttpServer(String authtoken, Integer port, String ssid, File web, String apisecret) {
        if (!web.exists()) {
            web.mkdir();
        }
        File htmlFile = new File(web, "index.html");
        try {
            if (htmlFile.createNewFile()) {
                //file didnt exist, and has now been created
                URL url = new URL("https://raw.githubusercontent.com/Relism/RCore-API/main/index.html");
                //download a fresh file
                fh.downloadFile(url, htmlFile);
            }
        } catch (IOException e) {
            msg.log("&cError creating files in web folder: " + e.getMessage());
        }
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    Map < String, Object > params =
                            (Map < String, Object > ) exchange.getAttribute("parameters");
                    // get the requested path from the exchange
                    String requestedPath = exchange.getRequestURI().getPath();
                    // check if the path is empty or "/" and if so, use the index.html file in the "web" folder
                    if (requestedPath.isEmpty() || requestedPath.equals("/")) {
                        requestedPath = "index.html";
                    }
                    // create a file object by appending the requested path to the "web" folder
                    File file = new File(web, requestedPath);
                    // check if the file exists and is not a directory
                    if (file.exists() && !file.isDirectory()) {
                        // read the file into a byte array
                        byte[] bytes = Files.readAllBytes(file.toPath());
                        // get the file extension
                        String fileExtension = requestedPath.substring(requestedPath.lastIndexOf(".") + 1);
                        // set the response headers
                        exchange.getResponseHeaders().set("Content-Type", getContentType(fileExtension));
                        exchange.sendResponseHeaders(200, bytes.length);
                        // write the response body
                        exchange.getResponseBody().write(bytes);
                    } else {
                        // file does not exist or is a directory, return a 404 error
                        String response = "404: File not found";
                        exchange.sendResponseHeaders(404, response.length());
                        exchange.getResponseBody().write(response.getBytes());
                    }
                    // close the response body
                    exchange.getResponseBody().close();
                }
                private String getContentType(String fileExtension) {
                    switch (fileExtension) {
                        case "html":
                            return "text/html";
                        case "css":
                            return "text/css";
                        case "js":
                            return "application/javascript";
                        case "png":
                            return "image/png";
                        case "jpeg":
                        case "jpg":
                            return "image/jpeg";
                        default:
                            return "application/octet-stream";
                    }
                }
            }).getFilters().add(new parameterFilter());

            server.createContext("/api", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    replyOK(exchange, "Running. Visit apidocs.relimc.com for more info");
                }
            }).getFilters().add(new parameterFilter());

            server.createContext("/api/reload", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    Map < String, Object > params =
                            (Map < String, Object > ) exchange.getAttribute("parameters");

                    if (params.get("secret") != null) {
                        if (params.get("secret").equals(apisecret)) {
                            msg.log("&b==> &dReceived Internal API Request to Reload the Plugin...");
                            rl.reload();
                            replyOK(exchange, "Reloaded");
                        } else {
                            replyERROR(exchange, "Wrong apisecret");
                        }
                    } else {
                        replyERROR(exchange, "You have to provide a valid apisecret");
                    }
                }
            }).getFilters().add(new parameterFilter());

            server.createContext("/api/super/reload", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    Map < String, Object > params =
                            (Map < String, Object > ) exchange.getAttribute("parameters");

                    if (params.get("ssid") != null) {
                        if (params.get("ssid").equals(ssid)) {
                            msg.log("&b==> &dReceived Super API Request to Reload the Plugin...");
                            rl.reload();
                            replyOK(exchange, "Reloaded");
                        } else {
                            replyERROR(exchange, "Wrong SSID");
                        }
                    } else {
                        replyERROR(exchange, "You have to provide the current valid SSID");
                    }
                }
            }).getFilters().add(new parameterFilter());

            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void renderHTML(HttpExchange HttpEx, File file) throws IOException {
        try {
            Headers responseHeaders = HttpEx.getResponseHeaders();
            responseHeaders.add("Content-Type", ("text/html"));
            HttpEx.sendResponseHeaders(200, file.length());
            try (OutputStream os = HttpEx.getResponseBody()) {
                Files.copy(file.toPath(), os);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replyOK(HttpExchange HttpEx, String response) throws IOException {
        try {
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

    private void replyERROR(HttpExchange HttpEx, String response) throws IOException {
        try {
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