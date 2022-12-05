package dev.relismdev.rcoresync.commands;

import dev.relismdev.rcoresync.api.dataHandler;
import dev.relismdev.rcoresync.utils.reloader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class reload implements CommandExecutor {

    public dataHandler dh = new dataHandler();
    public reloader rl = new reloader();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        rl.reload();
        return true;

    }
}
