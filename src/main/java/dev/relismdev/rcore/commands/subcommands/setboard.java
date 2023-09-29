package dev.relismdev.rcore.commands.subcommands;

import dev.relismdev.rcore.commands.subCommand;
import dev.relismdev.rcore.utils.msg;
import dev.relismdev.rcore.utils.scoreboardBuilder;
import dev.relismdev.rcore.worldManagement.worldReader;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class setboard extends subCommand {

    private static scoreboardBuilder sb = new scoreboardBuilder();
    private worldReader wr = new worldReader();

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
        JSONObject level = wr.getLevel((int) player.getX(), (int) player.getZ(), player.getWorld());
        String levelString = level.toString();
        msg.log(levelString);

        if(args.length > 1){
            String scoreboard = args[1];
            msg.send(player, "Trying to set your scoreboard to '" + scoreboard + "'");
            sb.display(player, scoreboard);
        } else {
            msg.send(player, "You have to specify the scoreboard.");
        }
    }
}
