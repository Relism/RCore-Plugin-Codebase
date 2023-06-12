package dev.relismdev.rcore.api;

import dev.relismdev.rcore.utils.misc;
import dev.relismdev.rcore.utils.randomGen;
import dev.relismdev.rcore.utils.msg;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class dataHandler {

    public static org.json.JSONObject configData = null;
    public static String ssid = null;
    public static String node = null;

    public void downloadConfig() throws ParseException {
        JSONObject jsonData = null;
        try {
            String encodedSsid = URLEncoder.encode(ssid, "UTF-8");
            jsonData = getData(node + "/getConfig?ssid=" + encodedSsid);
            if (jsonData.length() == 0) {
                msg.log("&aThe requested data seems to be null... did you initialize the plugin correctly?");
                jsonData = null;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        configData = jsonData;
    }

    public JSONObject getData(String url) throws IOException, InterruptedException {
        URI uri = URI.create(url);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = (HttpRequest) HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse<String> res = client.send(
                req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        JSONObject json = new JSONObject(res.body());
        return json;
    }

    public void pushData(String url) throws IOException, InterruptedException {
        URI uri = URI.create(url);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = (HttpRequest) HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse < String > res = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public JSONObject reqAPI(String url) {
        JSONObject jsonData = null;

        try {
            jsonData = getData(url);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return jsonData;
    }

    //parser
    public String configString(String query){
        String value;

        if(configData != null){
            value = (String) configData.get(query);
        } else {
            value = "Data is null, reinitialize the plugin.";
        }

        return value;
    }

    public Integer configInt(String query){
        String rawValue;
        Integer value;

        if(configData != null){
            rawValue = (String) configData.get(query);
            value = Integer.parseInt(rawValue);
        } else {
            value = null;
        }

        return value;
    }

    public boolean configBool(String query){
        if(configData != null){
            return "true".equals(configData.get(query));
        } else {
            return false;
        }
    }

    public void fetchSSID(String fetched){
        ssid = fetched;
    }

    public void fetchNode(String fetched){
        node = fetched;
    }

}
