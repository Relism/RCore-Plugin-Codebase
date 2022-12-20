package dev.relismdev.rcore.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class msg {
    public static void send(Player p, String message){
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    public static void broadcast(String message){
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    public static void log(String message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "• " + message));
    }
}
