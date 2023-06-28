package dev.relismdev.rcore.commands;

import dev.relismdev.rcore.commands.subcommands.setboard;
import dev.relismdev.rcore.utils.msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class commandManager implements CommandExecutor {

    private ArrayList<subCommand> subcommands = new ArrayList<>();

    public commandManager(){
        subcommands.add(new setboard());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player){
            Player player = (Player) sender;

            if(args.length > 0){
                for (int i = 0; i < getSubcommands().size(); i++){
                    if(args[0].equalsIgnoreCase(getSubcommands().get(i).getName())){
                        getSubcommands().get(i).perform(player, args);
                    }
                }
            } else {
                msg.send(player, "that's me, rcore");
            }
        }


        return true;
    }

    public ArrayList<subCommand> getSubcommands(){
        return subcommands;
    }
}
