package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.api.dataHandler;
import dev.relismdev.rcore.storage.localStorage;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class reloader {

    public dataHandler dh = new dataHandler();
    public localStorage ls = new localStorage();

    public boolean reload(){

        boolean state = false;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        long startTime = System.currentTimeMillis();

        try {
            msg.log("Starting to download data from the API");
            ls.storeConfig();
            if(ls.configData != null){
                state = true;
                JSONObject data = ls.configData;
                int dataEntries = data.length();
                long endTime = System.currentTimeMillis();
                double ETA = (endTime - startTime) / 1000;
                String speed;
                double speedValue = dataEntries/ETA;
                if(ETA <= 0){
                    speed = "Instantly!";
                } else {
                    speed = df.format(speedValue) + " entries/second";
                }
                msg.log("Successfully Downloaded &d" + dataEntries + " &6data entries from the API in &d" + ETA + " &6second(s)! (" + speed + ")");
            } else {
                msg.log("Data downloaded was null, make sure this plugin instance was properly initialized and that you actually own a valid authtoken...");
                msg.log("IGNORE THE FOLLOWING ERROR STACKTRACE, AS ITS JUST THE PLUGIN FORCEFULLY STOPPING ITSELF :P");
                state = false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return state;
    }
}
