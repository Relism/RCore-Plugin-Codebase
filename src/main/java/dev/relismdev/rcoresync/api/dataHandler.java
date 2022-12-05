package dev.relismdev.rcoresync.api;

import dev.relismdev.rcoresync.utils.randomGen;
import dev.relismdev.rcoresync.utils.msg;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class dataHandler {

    public static String authtoken;
    public static Integer port;
    public static JSONObject configData = null;

    public void downloadConfig() throws ParseException {
        HttpResponse<String> data = null;
        JSONObject jsonData = null;

        try { data = getData("https://api.relimc.com/rcore/getConfig?authtoken=" + authtoken);
            if(!data.toString().equals("{}")){
                try { jsonData = toObject(data.body()); }
                catch (ParseException e) { e.printStackTrace(); }
            } else {
                msg.log("&aThe requested data seems to be null... did you initialize the plugin correctly ?");
                jsonData = null;
            }
        }
        catch (IOException | InterruptedException e) { e.printStackTrace(); }
        configData = jsonData;
    }

    public HttpResponse < String > getData(String url) throws IOException, InterruptedException {
        URI uri = URI.create(url);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = (HttpRequest) HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse < String > res = client.send(
                req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        return res;
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

    public JSONObject toObject(String data) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(data);

        return json;
    }

    public String reqAPI(String url) {
        HttpResponse<String> data = null;

        try { data = getData(url); }
        catch (IOException | InterruptedException e) { e.printStackTrace(); }

        return data.body();
    }

    public String pushConfigData(String authtoken, Integer Port) {
        randomGen sg = new randomGen();
        String ssid = sg.generate(25);

        try { pushData("https://api.relimc.com/rcore/startPlugin?authtoken=" + authtoken + "&ssid=" + ssid + "&port=" + port); }
        catch (IOException | InterruptedException e) { e.printStackTrace(); }

        return ssid;
    }

    public void pushConfig(String token, Integer apiPort){
        authtoken = token;
        port = apiPort;
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
}
