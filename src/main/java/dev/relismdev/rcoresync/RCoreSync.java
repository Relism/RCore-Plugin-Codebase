package dev.relismdev.rcoresync;

import dev.relismdev.rcoresync.commands.reload;
import dev.relismdev.rcoresync.commands.getData;
import org.bukkit.plugin.java.JavaPlugin;
import dev.relismdev.rcoresync.api.*;
import dev.relismdev.rcoresync.utils.*;
import dev.relismdev.rcoresync.utils.msg;
import java.io.File;

public final class RCoreSync extends JavaPlugin {

    public String authtoken = getConfig().getString("authtoken");
    public String accept_terms = getConfig().getString("accept-terms");
    public Integer port = getConfig().getInt("port");
    public String apisecret = getConfig().getString("apisecret");

    public static RCoreSync plugin;

    //class handling
    public dataHandler dh = new dataHandler();
    public initializer init = new initializer();
    public authenticator auth = new authenticator();
    public appApi api = new appApi();

    //data handling
    public String ip = dh.configString("ip");

    @Override
    public void onEnable() {

        File webFolder = new File(getDataFolder(), "web");
        plugin = this;

        saveDefaultConfig();
        msg.log("&6[]────────────────[Starting RCore]────────────────[]");
        msg.log("───────────────────────────────────────────────────");
        dh.pushConfig(authtoken, port);

        //Setup Check
        if(authtoken != null){
            if(accept_terms != null && accept_terms.equals("true")){
                if(port != null && port != 0){
                    if(apisecret != null){
                        //authentication
                        msg.log("&bAuthenticating through the API...");
                        if(auth.authenticate(authtoken)){
                            //authenticated logic
                            msg.log("&aSuccessfully authenticated to the API!");
                            msg.log("───────────────────────────────────────────────────");
                            long startTime = System.currentTimeMillis();
                            //initialization
                            if(!init.initialize(authtoken, port, webFolder, apisecret)){
                                //initialization error handler
                                getServer().getPluginManager().disablePlugin(this);
                            } else {
                                //initialization success logic
                                long endTime = System.currentTimeMillis();
                                double ETA = (endTime - startTime) / 1000;
                                msg.log("&aInitialization Complete! Process took : &b" + ETA + " &asecond(s)");
                                msg.log("───────────────────────────────────────────────────");
                            }
                        } else {
                            //non-authenticated logic
                            msg.log("&cCouldnt authenticate correctly to the API, disabling the plugin.");
                            msg.log("&eHow to fix? If you purchased this plugin, &bvisit the dashboard and reinitialize it &eor &binitialize &eit first.");
                            msg.log("&eAlso doublecheck the &bauthtoken &ein the &bconfig.yml &eis the one you were provided on purchase, and that this server is running on the &bWhitelisted IP&e!");
                            msg.log("&eStill not working? Contact Relism on the discord support server : &bhttps://discord.gg/Np8t5MwHBU");
                            getServer().getPluginManager().disablePlugin(this);
                        }
                    } else {
                        //no apisecret logic
                        msg.log("&cSeems like you didnt setup an &bapisecret &cin the &bconfig.yml &cfile! Set it up properly and &brestart the server&c.");
                        getServer().getPluginManager().disablePlugin(this);
                    }
                } else {
                    //no port logic
                    msg.log("&cSeems like you didnt setup a &bport &cin the &bconfig.yml &cfile! Set it up properly and &brestart the server&c.");
                    getServer().getPluginManager().disablePlugin(this);
                }
            } else {
                //no terms accepted logic
                msg.log("&cYou have to accept &bRCore's api terms &d( https://apidocs.relimc.com ) &cin order to use this plugin!");
                msg.log("&cTo do so, set the &b'accept-terms' field &cin the &bconfig.yml &cfile to true and &brestart the server&c.");
                getServer().getPluginManager().disablePlugin(this);
            }
        } else {
            //no authtoken logic
            msg.log("&cSeems like you didnt setup the &bauthtoken &cin the config.yml file! Set it up properly and &brestart the server&c.");
            getServer().getPluginManager().disablePlugin(this);
        }

        getCommand("getdata").setExecutor(new getData());
        getCommand("reloadData").setExecutor(new reload());
    }

    @Override
    public void onDisable() {

        msg.log("&6[]────────────────[Stopping RCore]────────────────[]");

    }
}
