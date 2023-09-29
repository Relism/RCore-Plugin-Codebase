package dev.relismdev.rcore.api.contexts;

import com.sun.net.httpserver.HttpExchange;
import dev.relismdev.rcore.api.appApi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class rootContext {

    private appApi api;

    public rootContext(appApi api) {
        this.api = api;
    }

    public void perform(HttpExchange exchange, File web) throws IOException {
        Map< String, Object > params =
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
                return "image/jpeg";
            case "jpg":
                return "image/jpeg";
            default:
                return "application/octet-stream";
        }
    }
}
