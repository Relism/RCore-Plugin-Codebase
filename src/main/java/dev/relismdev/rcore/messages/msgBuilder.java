package dev.relismdev.rcore.messages;
import dev.relismdev.rcore.api.dataHandler;

public class msgBuilder {
    dataHandler dh = new dataHandler();

    public String discord(String sender, String message){
        String syntax = dh.configString("dstomcsyntax");
        syntax = syntax.replace("{sender}", sender);
        syntax = syntax.replace("{message}", message);
        return syntax;
    }

}
