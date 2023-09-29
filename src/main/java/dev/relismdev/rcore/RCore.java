package dev.relismdev.rcore;

import dev.relismdev.rcore.commands.commandManager;
import dev.relismdev.rcore.messages.consoleListener;
import dev.relismdev.rcore.messages.msgListener;
import dev.relismdev.rcore.proximityChat.voxelBuilder;
import dev.relismdev.rcore.worldManagement.worldManager;
import io.socket.client.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import dev.relismdev.rcore.api.*;
import dev.relismdev.rcore.utils.*;
import dev.relismdev.rcore.utils.msg;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

public final class RCore extends JavaPlugin {

    public String apisecret = getConfig().getString("apisecret");
    public Boolean autoupdate = getConfig().getBoolean("autoupdate");

    public static RCore plugin;

    //class handling
    public worldManager wm = new worldManager();
    public initializer init = new initializer(this);
    public appApi api = new appApi();
    public updater updater = new updater(this);
    public misc misc = new misc();
    private static Socket socket = socketHandler.socket;
    public Handler consoleHandler = new ConsoleHandler();
    public msgListener listener = new msgListener();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new voxelBuilder(), this);
        File webFolder = new File(getDataFolder(), "web");
        Boolean startPlugin;
        plugin = this;

        saveDefaultConfig();
        msg.log("&#22D3EE[]────────────────[Starting RCore]────────────────[]");
        long startTime = System.currentTimeMillis();
        //to write data abt rcore
        misc.printAsciiArt("&#22D3EE", "Standard", "Giufang");

        //String version = getFile().getName().replaceAll(".*(-\\d{4}-\\d{2}-\\d{2})\\..*", "$1");]
        String version = plugin.getPluginMeta().getVersion();

        msg.log(misc.separator("&#eba434", "UPDATER"));
        startPlugin = updater.run(version, autoupdate);
        msg.log("");

        msg.log(misc.separator("&#eb34b4", "CONFIG CHECK"));
        if(startPlugin && misc.checkConfig(this)) {
            msg.log("");
            /*msg.log(misc.separator("&#f5f542", "NODE-TEST"));
            dh.setNode(nodeTester.run(apinode));
            msg.log("");*/
            //DEPENDENCIES
            msg.log(misc.separator("&#34deeb", "DEPENDENCIES"));
            msg.log("Loading softdepends...");
            //Soft depends Check
            misc.checkSoftDep("PlaceholderAPI");
            msg.log("");

            //INITIALIZER
            msg.log(misc.separator("&#8634eb", "INITIALIZER"));
            //initialization
            if(!init.initialize(webFolder, apisecret)) {
                //initialization error handler
                getServer().getPluginManager().disablePlugin(this);
            } else {
                //initialization success logic
                long endTime = System.currentTimeMillis();
                double ETA = (endTime - startTime) / 1000;
                msg.log("&aStartup Complete! Process took : &b" + ETA + " &asecond(s)");
                msg.log("");
                Logger log = (Logger) LogManager.getRootLogger();
                log.addAppender(new consoleListener());
                //END INITIALIZER
                getCommand("rcore").setExecutor(new commandManager());
                getServer().getPluginManager().registerEvents(listener, this);
            }
        }
    }

    @Override
    public void onDisable() {
        msg.log("&#22D3EE[]────────────────[Stopping RCore]────────────────[]");
    }

    public static RCore getInstance() {
        return plugin;
    }
}
