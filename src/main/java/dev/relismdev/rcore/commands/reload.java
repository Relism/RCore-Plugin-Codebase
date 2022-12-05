package dev.relismdev.rcore.commands;

import dev.relismdev.rcore.api.dataHandler;
import dev.relismdev.rcore.utils.reloader;
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
