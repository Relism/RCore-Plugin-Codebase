package dev.relismdev.rcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import dev.relismdev.rcore.utils.scoreboardBuilder;


public class devStats implements CommandExecutor {
    private scoreboardBuilder builder = new scoreboardBuilder();

    private boolean bossBarVisible = false;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (bossBarVisible) {
            bossBarVisible = false;
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            builder.display(player, "off");
        } else {
            bossBarVisible = true;
            builder.display(player, "sb1");
        }
        return true;
    }
}