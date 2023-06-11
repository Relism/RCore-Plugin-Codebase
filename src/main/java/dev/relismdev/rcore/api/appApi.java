package dev.relismdev.rcore.api;

import dev.relismdev.rcore.RCore;
import dev.relismdev.rcore.utils.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.lang.management.*;
import java.net.InetSocketAddress;;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import dev.relismdev.rcore.utils.reloader;
import dev.relismdev.rcore.messages.*;
import dev.relismdev.rcore.messages.msgBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;

public class appApi {

    public reloader rl = new reloader();
    public fileHandler fh = new fileHandler();
    public misc misc = new misc(RCore.plugin);

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
                            return "image/jpeg";
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

            server.createContext("/api/sendMessage", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    Map < String, Object > params =
                            (Map < String, Object > ) exchange.getAttribute("parameters");

                    if (params.get("secret") != null) {
                        if (params.get("secret").equals(apisecret)) {
                            //handle the request
                            //msg.log("&b==> &dReceived Internal API Request for Server Information...");
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

            server.createContext("/api/getServerInfo", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    Map < String, Object > params =
                            (Map < String, Object > ) exchange.getAttribute("parameters");

                    if (params.get("secret") != null) {
                        if (params.get("secret").equals(apisecret)) {

                            //handle the request
                            msg.log("&b==> &dReceived Internal API Request for Server Information...");
                            //parse the process Uptime and put it in an obj
                            JSONObject uptime = new JSONObject();
                            RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
                            long[] uptimeUnits = convertMilliseconds(rb.getUptime());
                            //putting data in the obj
                            uptime.put("weeks", uptimeUnits[0]);
                            uptime.put("days", uptimeUnits[1]);
                            uptime.put("hours", uptimeUnits[2]);
                            uptime.put("minutes", uptimeUnits[3]);
                            uptime.put("seconds", uptimeUnits[4]);

                            //get the hardware info in an object
                            JSONObject hardware = new JSONObject();
                            //parse the memoryInfo
                            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
                            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
                            long maxMemory = heapUsage.getMax() / 1024 / 1024;
                            long usedMemory = heapUsage.getUsed() / 1024 / 1024;
                            long availableMemory = maxMemory - usedMemory;
                            // Get the operating system's CPU usage
                            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
                            Integer processors = osBean.getAvailableProcessors();
                            double avgLoad = osBean.getSystemLoadAverage();
                            //put hardware info in the object
                            hardware.put("allocated_ram", maxMemory);
                            hardware.put("used_ram", usedMemory);
                            hardware.put("free_ram", availableMemory);
                            hardware.put("processors", processors);
                            hardware.put("cpu_load", avgLoad);

                            //Get the plugin list in an array
                            Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
                            JSONArray pluginsArray = new JSONArray();
                            for (Plugin plugin : plugins) {
                                JSONObject pluginData = new JSONObject();
                                pluginData.put("name", plugin.getName());
                                pluginData.put("version", plugin.getDescription().getVersion());
                                pluginData.put("author", plugin.getDescription().getAuthors());
                                pluginData.put("enabled", plugin.isEnabled());
                                pluginsArray.put(pluginData);
                            }

                            //Get the online players names in an array
                            Collection < ? extends Player > onlinePlayers = Bukkit.getOnlinePlayers();
                            JSONArray playersArray = new JSONArray();
                            for (Player player : onlinePlayers) {
                                JSONObject playerData = new JSONObject();
                                playerData.put("username", player.getName());
                                playerData.put("uuid", player.getUniqueId());
                                playersArray.put(playerData);
                            }

                            Properties serverProperties = new Properties();
                            InputStream input = new FileInputStream("server.properties");
                            serverProperties.load(input);
                            input.close();

                            JSONObject serverPropertiesData = new JSONObject();
                            for (String key: serverProperties.stringPropertyNames()) {
                                String value = serverProperties.getProperty(key);
                                serverPropertiesData.put(key, value);
                            }

                            //create the JSON obj
                            JSONObject obj = new JSONObject();
                            obj.put("server_version", Bukkit.getBukkitVersion());
                            obj.put("hardware", hardware);
                            obj.put("uptime", uptime);
                            obj.put("player_count", onlinePlayers.size());
                            obj.put("players", playersArray);
                            obj.put("plugin_count", plugins.length);
                            obj.put("plugins", pluginsArray);
                            obj.put("properties", serverPropertiesData);

                            //return the JSON obj as response
                            replyOK(exchange, obj.toString(2));
                        } else {
                            replyERROR(exchange, "Wrong apisecret");
                        }
                    } else {
                        replyERROR(exchange, "You have to provide a valid apisecret");
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

    public static long[] convertMilliseconds(long ms) {
        // Calculate the number of weeks
        long weeks = ms / 1000 / 60 / 60 / 24 / 7;
        ms = ms - (weeks * 1000 * 60 * 60 * 24 * 7);
        // Calculate the number of days
        long days = ms / 1000 / 60 / 60 / 24;
        ms = ms - (days * 1000 * 60 * 60 * 24);
        // Calculate the number of hours
        long hours = ms / 1000 / 60 / 60;
        ms = ms - (hours * 1000 * 60 * 60);
        // Calculate the number of minutes
        long minutes = ms / 1000 / 60;
        ms = ms - (minutes * 1000 * 60);
        // Calculate the number of seconds
        long seconds = ms / 1000;
        // Return the result as an array
        return new long[] {
                weeks,
                days,
                hours,
                minutes,
                seconds
        };
    }

    private void replyOK(HttpExchange HttpEx, String response) throws IOException {
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

    private void replyERROR(HttpExchange HttpEx, String response) throws IOException {
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