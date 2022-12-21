package dev.relismdev.rcore.utils;

import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import java.util.regex.Pattern;

public class msg {

    public static final Pattern HEX_PATTERN = Pattern.compile("&(#[A-Fa-f0-9]{6})");
    public static final char COLOR_CHAR = ChatColor.COLOR_CHAR;

    public static void send(Player p, String message) {
        p.sendMessage(translateColorCodes(message));
    }
    public static void broadcast(String message) {
        //Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
        Bukkit.broadcastMessage(translateColorCodes(message));
    }
    public static void log(String message) {
        //Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "• " + message));
        Bukkit.getConsoleSender().sendMessage(translateColorCodes(message));
    }
    public static String translateColorCodes(String text) {
        final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";
        String[] texts = text.split(String.format(WITH_DELIMITER, "&"));
        StringBuilder finalText = new StringBuilder();
        for (int i = 0; i < texts.length; i++) {
            if (texts[i].equalsIgnoreCase("&")) {
                //get the next string
                i++;
                if (texts[i].charAt(0) == '#') {
                    finalText.append(net.md_5.bungee.api.ChatColor.of(texts[i].substring(0, 7)) + texts[i].substring(7));
                } else {
                    finalText.append(ChatColor.translateAlternateColorCodes('&', "&" + texts[i]));
                }
            } else {
                finalText.append(texts[i]);
            }
        }
        return finalText.toString();
    }
}