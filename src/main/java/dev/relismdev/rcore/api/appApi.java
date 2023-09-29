package dev.relismdev.rcore.api;

import dev.relismdev.rcore.api.middlewares.authMiddleware;
import dev.relismdev.rcore.storage.playerStorage;
import dev.relismdev.rcore.utils.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import dev.relismdev.rcore.utils.reloader;
import dev.relismdev.rcore.api.contexts.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class appApi {


    public reloader rl = new reloader();
    public fileHandler fh = new fileHandler();

    private rootContext rootContext = new rootContext(this);
    private sendMessageContext sendMessageContext = new sendMessageContext(this);
    private getServerInfoContext getServerInfoContext = new getServerInfoContext(this);


    public void startHttpServer(Integer port, String ssid, File web, String apisecret) {
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
            authMiddleware amw = new authMiddleware(apisecret);
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    rootContext.perform(exchange, web);
                }
            }).getFilters().add(new parameterFilter());

            server.createContext("/sse", new sseHandler());

            server.createContext("/api", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if(amw.handle(exchange)){
                        replyOK(exchange, "Running. Visit apidocs.relimc.com for more info");
                    }
                }
            }).getFilters().add(new parameterFilter());

            server.createContext("/api/sendMessage", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if(amw.handle(exchange)){
                        sendMessageContext.perform(exchange);
                    }
                }
            }).getFilters().add(new parameterFilter());

            server.createContext("/api/getServerInfo", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if(amw.handle(exchange)){
                        getServerInfoContext.perform(exchange);
                    }
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

            server.createContext("/api/getPlayer", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    Map < String, Object > params =
                            (Map < String, Object > ) exchange.getAttribute("parameters");

                    if (params.get("secret") != null) {
                        if (params.get("secret").equals(apisecret)) {
                            String playerName = params.get("player").toString();
                            Player player = Bukkit.getPlayer("playerName");
                            // Get the player's data file
                            File playerDataFile = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata/" + player.getUniqueId() + ".dat");

                            // Read the file into a byte array
                            byte[] bytes = new byte[0];
                            try {
                                bytes = Files.readAllBytes(playerDataFile.toPath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // Parse the byte array into a JSONObject
                            JSONObject playerData = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
                            JSONObject obj = new JSONObject();
                            obj.put(playerName, playerData.toString(2));
                        } else {
                            replyERROR(exchange, "Wrong apisecret");
                        }
                    } else {
                        replyERROR(exchange, "You have to provide a valid apisecret");
                    }
                }
            }).getFilters().add(new parameterFilter());

            server.createContext("/api/sendMessage", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    Map < String, Object > params =
                            (Map < String, Object > ) exchange.getAttribute("parameters");

                    if (params.get("secret") != null) {
                        if (params.get("secret").equals(apisecret)) {
                            //handle the request
                            String sender = params.get("sender").toString();
                            String message = params.get("message").toString();
                            replyOK(exchange, "message_sent");
                        } else {
                            replyERROR(exchange, "Wrong apisecret");
                        }
                    } else {
                        replyERROR(exchange, "You have to provide a valid apisecret");
                    }
                }
            }).getFilters().add(new parameterFilter());

            server.createContext("/api/super/sendMessage", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    Map < String, Object > params =
                            (Map < String, Object > ) exchange.getAttribute("parameters");

                    if (params.get("ssid") != null){
                        if (params.get("ssid").equals(ssid)) {
                            //handle the request
                            String sender = params.get("sender").toString();
                            String message = params.get("message").toString();
                            replyOK(exchange, "message_sent");
                        } else {
                            replyERROR(exchange, "Wrong apisecret");
                        }
                    } else {
                        replyERROR(exchange, "You have to provide the current valid SSID");
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

  /*private void renderHTML(HttpExchange HttpEx, File file) throws IOException {
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
  }*/

    private String msToDays(Long ms) {
        // First, convert the milliseconds value to seconds
        long seconds = ms / 1000;

        // Next, calculate the number of days, hours, minutes, and seconds
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = ((seconds % 86400) % 3600) / 60;
        long remainingSeconds = ((seconds % 86400) % 3600) % 60;

        // Return the result as a string
        return days + " days, " + hours + " hours, " + minutes + " minutes, " + remainingSeconds + " seconds";
    }

    public void replyOK(HttpExchange HttpEx, String response) throws IOException {
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

    public void replyERROR(HttpExchange HttpEx, String response) throws IOException {
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