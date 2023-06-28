package dev.relismdev.rcore.commands;

import org.bukkit.entity.Player;

public abstract class subCommand {

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getSyntax();

    public abstract void perform(Player player, String args[]);

}
