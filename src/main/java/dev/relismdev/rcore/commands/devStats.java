package dev.relismdev.rcore.commands;

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
    private BossBar bossBar;
    private boolean bossBarVisible = false;
    private Timer timer;
    private scoreboardBuilder builder = new scoreboardBuilder();
    private Scoreboard scoreboard;

    public devStats() {
        this.bossBar = Bukkit.createBossBar("Title", BarColor.RED, BarStyle.SOLID);
    }

    private void updateBossBarData(Player player) {
        World world = player.getWorld(); // get the world that the player is currently in
        long tickTime = world.getTime() % 24000; // get the current tick time
        double progress = tickTime / 24000.0; // calculate the progress as a value between 0 and 1
        String timeOfDay = String.format("%02d:%02d", (int)(tickTime * 24 / 24000.0), (int)((tickTime * 24 * 60 / 24000.0) % 60)); // convert the tick time to a time of day string
        String title = world.getName() + " - " + timeOfDay; // concatenate the world name and time of day strings
        bossBar.setTitle(title); // set the boss bar title
        bossBar.setProgress(progress); // set the boss bar progress
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;
        JSONObject config = new JSONObject("{\n" +
                "  \"title\": \"My Scoreboard\",\n" +
                "  \"1\": 10,\n" +
                "  \"2\": 20,\n" +
                "  \"3\": 30\n" +
                "}");
        scoreboard = builder.build(config);

        if (bossBarVisible) {
            bossBarVisible = false;
            bossBar.removePlayer(player);
            if (timer != null) {
                timer.cancel();
            }
            player.sendMessage("DevStats boss bar and scoreboard hidden.");
        } else {
            bossBarVisible = true;
            bossBar.addPlayer(player);
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateBossBarData(player);
                }
            }, 0, 1000);
            player.setScoreboard(scoreboard);
            player.sendMessage("DevStats boss bar and scoreboard shown.");
        }
        return true;
    }
}