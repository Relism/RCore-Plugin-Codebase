package dev.relismdev.rcore;

import dev.relismdev.rcore.commands.devStats;
import dev.relismdev.rcore.commands.reload;
import dev.relismdev.rcore.commands.getData;
import dev.relismdev.rcore.messages.msgListener;
import io.socket.client.Socket;
import org.bukkit.plugin.java.JavaPlugin;
import dev.relismdev.rcore.api.*;
import dev.relismdev.rcore.utils.*;
import dev.relismdev.rcore.utils.msg;
import org.json.JSONObject;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RCore extends JavaPlugin {

    public String authtoken = getConfig().getString("authtoken");
    public String apisecret = getConfig().getString("apisecret");
    public Boolean autoupdate = getConfig().getBoolean("autoupdate");
    public String ssid = getConfig().getString("ssid");
    public String apinode = getConfig().getString("node");

    public static RCore plugin;

    //class handling
    public dataHandler dh = new dataHandler();
    public initializer init = new initializer(this);
    public appApi api = new appApi();
    public updater updater = new updater(this);
    public misc misc = new misc();
    private static Socket socket = SocketHandler.socket;
    public Handler consoleHandler = new ConsoleHandler();
    public msgListener listener = new msgListener();
    public nodeTester nodeTester = new nodeTester();

    @Override
    public void onEnable() {
        // Create and add the console handler
        consoleHandler.setLevel(Level.ALL);
        Logger logger = Logger.getLogger("");
        logger.addHandler(consoleHandler);

        JSONObject authdata = new JSONObject();
        authdata.put("authtoken", authtoken);
        authdata.put("ssid", ssid);

        File webFolder = new File(getDataFolder(), "web");
        Boolean startPlugin;
        plugin = this;

        saveDefaultConfig();
        msg.log("&#22D3EE[]────────────────[Starting RCore]────────────────[]");
        long startTime = System.currentTimeMillis();
        //to write data abt rcore
        misc.printAsciiArt("&#22D3EE", "Standard", "RCore");

        String version = getFile().getName().replaceAll(".*(-\\d{4}-\\d{2}-\\d{2})\\..*", "$1");

        msg.log(misc.separator("&#eba434", "UPDATER"));
        startPlugin = updater.run(version, autoupdate);
        msg.log("");

        msg.log(misc.separator("&#f5f542", "NODE-TEST"));
        dh.setNode(nodeTester.run(apinode));
        msg.log("");

        msg.log(misc.separator("&#eb34b4", "CONFIG CHECK"));
        if(startPlugin && misc.checkConfig(this)) {
            msg.log("");
            //DEPENDENCIES
            msg.log(misc.separator("&#34deeb", "DEPENDENCIES"));
            msg.log("Loading softdepends...");
            //Soft depends Check
            misc.checkSoftDep("PlaceholderAPI");
            msg.log("");

            //INITIALIZER
            msg.log(misc.separator("&#8634eb", "INITIALIZER"));
            //initialization
            if(!init.initialize(webFolder, apisecret, authdata)) {
                //initialization error handler
                getServer().getPluginManager().disablePlugin(this);
            } else {
                //initialization success logic
                long endTime = System.currentTimeMillis();
                double ETA = (endTime - startTime) / 1000;
                msg.log("&aStartup Complete! Process took : &b" + ETA + " &asecond(s)");
                msg.log("");
                //END INITIALIZER
                getCommand("getdata").setExecutor(new getData());
                getCommand("reloadData").setExecutor(new reload());
                getCommand("devstats").setExecutor(new devStats());
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
