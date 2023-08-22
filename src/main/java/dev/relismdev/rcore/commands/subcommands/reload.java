package dev.relismdev.rcore.commands.subcommands;

import dev.relismdev.rcore.commands.subCommand;
import dev.relismdev.rcore.utils.reloader;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class reload extends subCommand {

    public reloader rl = new reloader();

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload the plugin data";
    }

    @Override
    public String getSyntax() {
        return "/rcore reload";
    }

    @Override
    public void perform(Player player, String[] args) {
        executor.execute(() -> {
            rl.reload();
        });
    }
}