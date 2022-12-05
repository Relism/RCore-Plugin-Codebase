package dev.relismdev.rcoresync.commands;

import dev.relismdev.rcoresync.api.dataHandler;
import dev.relismdev.rcoresync.utils.msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class getData implements CommandExecutor {

    public dataHandler dh = new dataHandler();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        msg.log("&bData for &d" + args[0] + " &b: &d" + dh.configString(args[0]));

        /*if (sender instanceof ConsoleCommandSender) {
            String data = configData(args[0]);
            sender.sendMessage(data);
            System.out.println(data);
        }*/

        return true;
    }
}
