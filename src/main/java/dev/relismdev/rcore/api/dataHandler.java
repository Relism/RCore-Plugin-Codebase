package dev.relismdev.rcore.api;

import dev.relismdev.rcore.utils.msg;
import org.json.JSONObject;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class dataHandler {

    public static String ssid = null;
    public static String node = null;

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

    public JSONObject reqAPI(String url) {
        JSONObject jsonData = null;
        try {
            jsonData = getData(url);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return jsonData;
    }


    public void setSSID(String fetched){
        ssid = fetched;
    }

    public void setNode(String fetched){
        node = fetched;
    }

    public String getSSID(){
        return ssid;
    }

    public String getNode(){
        return node;
    }

}
