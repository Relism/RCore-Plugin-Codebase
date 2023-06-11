package dev.relismdev.rcore.utils;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.*;
import org.json.JSONObject;

public class scoreboardBuilder {

    public Scoreboard build(JSONObject config){
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();

        Objective title = scoreboard.registerNewObjective("title", "dummy", config.getString("title"));
        title.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (String key : config.keySet()) {
            if (!key.equals("title")) {
                Score row = title.getScore(key);
            }
        }
        return scoreboard;
    }

}