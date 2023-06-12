package dev.relismdev.rcore.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class scoreboardBuilder {

    public Scoreboard[] build(JSONObject rawconfig, Player player) {

        Integer frames = 0;
        for (String key : rawconfig.keySet()) {
            JSONArray array = rawconfig.getJSONArray(key);
            frames = Math.max(frames, array.length());
        }

        JSONObject config = sanitizeFrames(rawconfig, frames);

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard[] scoreboards = new Scoreboard[frames];

        for (Integer i = 0; i < frames; i++) {
            scoreboards[i] = manager.getNewScoreboard();

            String titleValue = msg.translateColorCodes(PlaceholderAPI.setPlaceholders(player, config.getJSONArray("title").getString(i)));
            Objective title = scoreboards[i].registerNewObjective("title", "dummy", titleValue);
            title.setDisplaySlot(DisplaySlot.SIDEBAR);

            for (String key : config.keySet()) {
                if (!key.equals("title")) {
                    String rowValue = msg.translateColorCodes(PlaceholderAPI.setPlaceholders(player, config.getJSONArray(key).getString(i)));
                    Score row = title.getScore(rowValue);
                    row.setScore(i); // set the score for the row
                }
            }
        }

        return scoreboards; // return the created scoreboards
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