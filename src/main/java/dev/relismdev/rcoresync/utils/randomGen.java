package dev.relismdev.rcoresync.utils;

import java.security.SecureRandom;

public class randomGen {

    public String generate(int len){
        final String chars = "0123456789abcdefghijklmnopqrstuvwxyz!@$*()ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++){
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }
}
