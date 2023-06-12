package dev.relismdev.rcore.utils;

import dev.relismdev.rcore.api.dataHandler;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class scoreboardBuilder {

    private static Timer timer;
    public dataHandler dh = new dataHandler();
    public misc misc = new misc();

    public JSONObject compile(JSONObject config, Player player) {
        JSONObject compiled = new JSONObject();

        Integer scoreboards = 0;
        for (String key : config.keySet()) {
            msg.log("scoreboard : " + key);

            JSONObject rawconfig = config.getJSONObject(key);

            msg.log("config : " + rawconfig.toString());

            JSONObject animation = (JSONObject) rawconfig.get("animation");
            Scoreboard[] scoreboardAnimation = build(animation, player);

            JSONObject sbObject = new JSONObject();
            sbObject.put("animation", scoreboardAnimation);
            sbObject.put("speed", rawconfig.getInt("speed"));

            compiled.put(key, sbObject);
            scoreboards++;
        }

        msg.log("Compiled " + scoreboards + " scoreboards together.");
        return compiled;
    }

    public Scoreboard[] build(JSONObject rawconfig, Player player) {

        Integer frames = 0;
        Integer rows = 0;
        for (String key : rawconfig.keySet()) {
            JSONArray array = rawconfig.getJSONArray(key);
            frames = Math.max(frames, array.length());
            if(!key.equals("title")){
                rows++;
            }
        }

        msg.log("building scoreboard for " + player.getName() + " @" + frames + " frames");

        JSONObject config = sanitizeFrames(rawconfig, frames);

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard[] scoreboardAnimation = new Scoreboard[frames];

        for (Integer i = 0; i < frames; i++) {
            scoreboardAnimation[i] = manager.getNewScoreboard();

            String titleValue = msg.translateColorCodes(PlaceholderAPI.setPlaceholders(player, config.getJSONArray("title").getString(i)));
            msg.log("titlevalue : " + titleValue);
            Objective title = scoreboardAnimation[i].registerNewObjective("title", "dummy", titleValue);
            title.setDisplaySlot(DisplaySlot.SIDEBAR);

            for (String key : config.keySet()) {
                if (!key.equals("title")) {
                    String rowValue = msg.translateColorCodes(PlaceholderAPI.setPlaceholders(player, config.getJSONArray(key).getString(i)));
                    Score row = title.getScore(rowValue);
                    row.setScore(rows - Integer.parseInt(key));
                }
            }
        }

        return scoreboardAnimation; // return the created scoreboards
    }

    public void display(Player player, String scoreboardName) {
        JSONObject configJson = new JSONObject(dh.configData.toString());
        JSONObject scoreboardsJson = configJson.getJSONObject("scoreboards");
        if (scoreboardName.equals("off")) {
            // If the scoreboard name is "off", cancel the timer and remove the scoreboard
            if (timer != null) {
                timer.cancel();
            }
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            return;
        }

        JSONObject sbAnimations = compile(scoreboardsJson, player);
        JSONObject scoreboard = (JSONObject) sbAnimations.get(scoreboardName);
        Scoreboard[] animation = (Scoreboard[]) scoreboard.get("animation");
        Number speedValue = (Number) scoreboard.get("speed");
        int delay = Math.round(1000 / (speedValue.floatValue() / 10));
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int sb = 0;
            @Override
            public void run() {
                player.setScoreboard(animation[sb]);
                sb = (sb + 1) % animation.length;
            }
        }, 0, delay);
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