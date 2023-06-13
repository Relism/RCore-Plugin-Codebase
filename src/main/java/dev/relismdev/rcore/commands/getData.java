package dev.relismdev.rcore.commands;

import dev.relismdev.rcore.api.dataHandler;
import dev.relismdev.rcore.storage.playerStorage;
import dev.relismdev.rcore.utils.msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class getData implements CommandExecutor {

    public dataHandler dh = new dataHandler();
    public playerStorage ps = new playerStorage();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //msg.log("&bData for &d" + args[0] + " &b: &d" + dh.configString(args[0]));

        Player player = (Player) sender;
        ps.get(player, "test");
        /*if (sender instanceof ConsoleCommandSender) {
            String data = configData(args[0]);
            sender.sendMessage(data);
            System.out.println(data);
        }*/

        return true;
    }
}
