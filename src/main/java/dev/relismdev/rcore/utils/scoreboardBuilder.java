package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.RCore;
import dev.relismdev.rcore.api.dataHandler;
import dev.relismdev.rcore.api.playerStorage;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class scoreboardBuilder {

    private static Timer timer;
    public dataHandler dh = new dataHandler();
    public misc misc = new misc();
    public playerStorage ps = new playerStorage();

    Plugin plugin = RCore.getInstance();

    public JSONObject frameExtractor(JSONObject rawconfig, int frame) {
        int frames = 0;
        for (String key : rawconfig.keySet()) {
            JSONArray array = rawconfig.getJSONArray(key);
            frames = Math.max(frames, array.length());
        }
        JSONObject config = sanitizeFrames(rawconfig, frames);

        JSONObject rebuiltConfig = new JSONObject();
        for (String key : config.keySet()) {
            JSONArray array = config.getJSONArray(key);
            rebuiltConfig.put(key, array.get(frame)); // Add only the common position element
        }

        return rebuiltConfig;
    }

    public int frameCalculator(JSONObject rawconfig){
        int frames = 0;
        for (String key : rawconfig.keySet()) {
            JSONArray array = rawconfig.getJSONArray(key);
            frames = Math.max(frames, array.length());
        }
        return frames;
    }

    public JSONObject parser(String scoreboardName){
        JSONObject configJson = new JSONObject(dh.configData.toString());
        JSONObject scoreboardsJson = configJson.getJSONObject("scoreboards");
        JSONObject scoreboard = (JSONObject) scoreboardsJson.get(scoreboardName);
        return scoreboard;
    }

    public Scoreboard bundler(JSONObject frame, Player player){
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();

        Integer rows = 0;
        for (String key : frame.keySet()) {
            if(!key.equals("title")){
                rows++;
            }
        }

        String titleValue = msg.translateColorCodes(PlaceholderAPI.setPlaceholders(player, frame.getString("title")));
        Objective title = scoreboard.registerNewObjective("title", "dummy", titleValue);
        title.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (String key : frame.keySet()) {
            if (!key.equals("title")) {
                String rowValue = msg.translateColorCodes(PlaceholderAPI.setPlaceholders(player, frame.getString(key)));
                Score row = title.getScore(rowValue);
                row.setScore(rows - Integer.parseInt(key));
            }
        }
        return scoreboard;
    }

    public void display(Player player, String scoreboardName) {
        if (scoreboardName.equals("off")) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            Bukkit.getScheduler().cancelTask(Integer.parseInt(ps.get(player, "scoreboard-taskid").getString("value")));
            return;
        }

        JSONObject scoreboard = parser(scoreboardName);
        JSONObject animation = scoreboard.getJSONObject("animation");
        int frames = frameCalculator(animation);
        int speed = scoreboard.getInt("speed");
        int tickDelay = 20 / speed;

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int sb = 0;
            @Override
            public void run() {
                JSONObject frameObject = frameExtractor(animation, sb);
                Scoreboard frame = bundler(frameObject, player);
                player.setScoreboard(frame);
                sb = (sb + 1) % frames;
            }
        }, 0L, tickDelay).getTaskId();
        ps.set(player, "scoreboard-taskid", String.valueOf(taskId));
    }

    public JSONObject sanitizeFrames(JSONObject config, Integer frames) {
        JSONObject sanitizedConfig = new JSONObject();

        // Pad each array with the last value until it has the same number of frames as the maximum number of frames
        for (String key : config.keySet()) {
            JSONArray array = config.getJSONArray(key);
            JSONArray sanitizedArray = new JSONArray();
            for (int i = 0; i < frames; i++) {
                if (i < array.length()) {
                    sanitizedArray.put(array.get(i));
                } else {
                    sanitizedArray.put(array.get(array.length() - 1));
                }
            }
            sanitizedConfig.put(key, sanitizedArray);
        }

        return sanitizedConfig;
    }
}