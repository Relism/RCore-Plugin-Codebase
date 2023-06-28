package dev.relismdev.rcore.commands.subcommands;

import dev.relismdev.rcore.commands.subCommand;
import dev.relismdev.rcore.utils.msg;
import dev.relismdev.rcore.utils.scoreboardBuilder;
import org.bukkit.entity.Player;

public class setboard extends subCommand {

    private static scoreboardBuilder sb = new scoreboardBuilder();

    @Override
    public String getName() {
        return "setboard";
    }

    @Override
    public String getDescription() {
        return "Set a given scoreboard";
    }

    @Override
    public String getSyntax() {
        return "/rcore setboard <scoreboard>";
    }

    @Override
    public void perform(Player player, String[] args) {
        if(args.length > 1){
            String scoreboard = args[1];
            msg.send(player, "Trying to set your scoreboard to '" + scoreboard + "'");
            sb.display(player, scoreboard);
        } else {
            msg.send(player, "There seems to be an issue with your command syntax, did you provide the scoreboard ?");
        }
    }
}
