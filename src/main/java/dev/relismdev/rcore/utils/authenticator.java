package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.api.dataHandler;

public class authenticator {

    public dataHandler dh = new dataHandler();
    public boolean authenticate(String authtoken){
        String res = dh.reqAPI("https://api.relimc.com/rcore/authenticate/?authtoken=" + authtoken);
            if(res.equals("Not Authenticated")){
                msg.log("&cAUTHENTICATION ERROR : &eBoth your ip and provided authtoken couldn't be found on the Authentication API.");
            } else if (res.equals("Token/IP mismatch")){
                msg.log("&cAUTHENTICATION ERROR : &eThe provided authtoken doesn't match your current ip");
            }
        return(res.equals("Authenticated"));
    }
}
