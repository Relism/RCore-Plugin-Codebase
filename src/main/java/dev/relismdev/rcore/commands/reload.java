package dev.relismdev.rcore.commands;

import dev.relismdev.rcore.utils.reloader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class reload implements CommandExecutor {

    public reloader rl = new reloader();

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        executor.execute(() -> {
            rl.reload();
        });
        return true;

    }
}
