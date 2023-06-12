package dev.relismdev.rcore.commands;

import dev.relismdev.rcore.utils.msg;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import dev.relismdev.rcore.utils.scoreboardBuilder;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.json.JSONObject;

public class devStats implements CommandExecutor {
    private BossBar bossBar = Bukkit.createBossBar("Title", BarColor.RED, BarStyle.SOLID);
    private Timer timer;
    private scoreboardBuilder builder = new scoreboardBuilder();
    private Scoreboard[] scoreboards;

    private boolean bossBarVisible = false;

    private void updateBossBarData(Player player) {
        bossBar.setTitle("");
        bossBar.setProgress(1);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;
        JSONObject config = new JSONObject("{\n" +
                "  \"title\": [\n" +
                "    \"&lA\",\n" +
                "    \"&lAn\",\n" +
                "    \"&lAni\",\n" +
                "    \"&lAnim\",\n" +
                "    \"&lAnima\",\n" +
                "    \"&lAnimat\",\n" +
                "    \"&lAnimate\",\n" +
                "    \"&lAnimated\",\n" +
                "    \"&lAnimated \",\n" +
                "    \"&lAnimated S\",\n" +
                "    \"&lAnimated Sc\",\n" +
                "    \"&lAnimated Sco\",\n" +
                "    \"&lAnimated Scor\",\n" +
                "    \"&lAnimated Score\",\n" +
                "    \"&lAnimated Scoreb\",\n" +
                "    \"&lAnimated Scorebo\",\n" +
                "    \"&lAnimated Scoreboa\",\n" +
                "    \"&lAnimated Scoreboar\",\n" +
                "    \"&lAnimated Scoreboard\",\n" +
                "    \"&c&lAnimated Scoreboard\",\n" +
                "    \"&lAnimated Scoreboard\",\n" +
                "    \"&c&lAnimated Scoreboard\",\n" +
                "    \"&lAnimated Scoreboard\",\n" +
                "    \"&c&lAnimated Scoreboard\"\n" +
                "  ],\n" +
                "  \"1\": [\n" +
                "    \"&a&lPlayer Kills: %player_name%\",\n" +
                "    \"&a&lPlayer Kills: %player_name%\",\n" +
                "    \"&a&lPlayer Kills: %player_name%\",\n" +
                "    \"&a&lPlayer Kills: %player_name%\",\n" +
                "    \"&a&lPlayer Kills: %player_name%\",\n" +
                "    \"&a&lPlayer Kills: %player_name%\",\n" +
                "    \"&e&lPlayer Deaths: 1\",\n" +
                "    \"&e&lPlayer Deaths: 1\",\n" +
                "    \"&e&lPlayer Deaths: 1\",\n" +
                "    \"&e&lPlayer Deaths: 1\",\n" +
                "    \"&e&lPlayer Deaths: 1\",\n" +
                "    \"&e&lPlayer Deaths: 1\",\n" +
                "    \"&c&lPlayer Score: 100\",\n" +
                "    \"&c&lPlayer Score: 100\",\n" +
                "    \"&c&lPlayer Score: 100\",\n" +
                "    \"&c&lPlayer Score: 100\",\n" +
                "    \"&c&lPlayer Score: 100\",\n" +
                "    \"&c&lPlayer Score: 100\",\n" +
                "    \"&6&lPlayer Level: 5\",\n" +
                "    \"&6&lPlayer Level: 5\",\n" +
                "    \"&6&lPlayer Level: 5\",\n" +
                "    \"&6&lPlayer Level: 5\",\n" +
                "    \"&6&lPlayer Level: 5\",\n" +
                "    \"&6&lPlayer Level: 5\"\n" +
                "  ],\n" +
                "  \"2\": [\n" +
                "    \"&a&lTeam Kills: 10\",\n" +
                "    \"&a&lTeam Kills: 10\",\n" +
                "    \"&a&lTeam Kills: 10\",\n" +
                "    \"&a&lTeam Kills: 10\",\n" +
                "    \"&a&lTeam Kills: 10\",\n" +
                "    \"&a&lTeam Kills: 10\",\n" +
                "    \"&e&lTeam Deaths: 5\",\n" +
                "    \"&e&lTeam Deaths: 5\",\n" +
                "    \"&e&lTeam Deaths: 5\",\n" +
                "    \"&e&lTeam Deaths: 5\",\n" +
                "    \"&e&lTeam Deaths: 5\",\n" +
                "    \"&e&lTeam Deaths: 5\",\n" +
                "    \"&c&lTeam Score: 500\",\n" +
                "    \"&c&lTeam Score: 500\",\n" +
                "    \"&c&lTeam Score: 500\",\n" +
                "    \"&c&lTeam Score: 500\",\n" +
                "    \"&c&lTeam Score: 500\",\n" +
                "    \"&c&lTeam Score: 500\",\n" +
                "    \"&6&lTeam Members: 5\",\n" +
                "    \"&6&lTeam Members: 5\",\n" +
                "    \"&6&lTeam Members: 5\",\n" +
                "    \"&6&lTeam Members: 5\",\n" +
                "    \"&6&lTeam Members: 5\",\n" +
                "    \"&6&lTeam Members: 5\"\n" +
                "  ]\n" +
                "}");

        scoreboards = builder.build(config, player);

        if (bossBarVisible) {
            bossBarVisible = false;
            bossBar.removePlayer(player);
            if (timer != null) {
                timer.cancel();
            }
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            player.sendMessage("DevStats boss bar and scoreboard hidden.");
        } else {
            bossBarVisible = true;
            bossBar.addPlayer(player);
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                int sb = 0; // initialize the scoreboard index to 0

                @Override
                public void run() {
                    updateBossBarData(player); // update the boss bar data
                    player.setScoreboard(scoreboards[sb]); // set the player's scoreboard to the current scoreboard
                    sb = (sb + 1) % scoreboards.length; // increment the scoreboard index and wrap around to the beginning if necessary
                }
            }, 0, 500);
            player.sendMessage("DevStats boss bar and scoreboard shown.");
        }
        return true;
    }
}