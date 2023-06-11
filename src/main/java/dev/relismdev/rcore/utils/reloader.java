package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.api.dataHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class reloader {

    public dataHandler dh = new dataHandler();

    public boolean reload(){

        boolean state = false;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        try {
            msg.log("&e<== &#a8328cStarting to download data from the API");
            long startTime = System.currentTimeMillis();
            dh.downloadConfig();
            msg.log(dh.configData.toJSONString());
            long endTime = System.currentTimeMillis();
            if(dh.configString("client") != null){
                state = true;
                JSONObject data = dh.configData;
                Integer dataEntries = data.size() - 1;
                double ETA = (endTime - startTime) / 1000;
                String speed;
                double speedValue = dataEntries/ETA;
                if(ETA <= 0){
                    speed = "Instantly!";
                } else {
                    speed = df.format(speedValue) + " entries/second";
                }
                msg.log("&#a8328cSuccessfully Downloaded &d" + dataEntries + " &6data entries from the API in &d" + ETA + " &6second(s)! (" + speed + ")");
            } else {
                msg.log("&cData downloaded was null, make sure this plugin instance was properly initialized and that you actually own a valid authtoken...");
                msg.log("&eIGNORE THE FOLLOWING ERROR STACKTRACE, AS ITS JUST THE PLUGIN FORCEFULLY STOPPING ITSELF :P");
                state = false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return state;
    }
}
