package dev.relismdev.rcore.worldManagement;

import dev.relismdev.rcore.utils.misc;
import dev.relismdev.rcore.utils.msg;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.File;

public class worldManager {

    private static misc misc = new misc();

    public boolean createWorld(JSONObject worldData) {
        String name = worldData.getString("name");
        WorldType worldType = WorldType.valueOf(worldData.getString("type").toUpperCase());
        boolean generateStructures = worldData.getBoolean("generateStructures");
        boolean allowMonsters = worldData.optBoolean("allowMonsters", true);
        boolean allowAnimals = worldData.optBoolean("allowAnimals", true);
        double spawnX = worldData.optDouble("spawnX", 0);
        double spawnY = worldData.optDouble("spawnY", 64);
        double spawnZ = worldData.optDouble("spawnZ", 0);

        WorldCreator worldCreator = new WorldCreator(name);
        worldCreator.type(worldType);
        worldCreator.generateStructures(generateStructures);

        World world = worldCreator.createWorld();

        if (world != null) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setSpawnLocation((int) spawnX, (int) spawnY, (int) spawnZ);
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, allowMonsters);
            world.setTime(6000);
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(1000000);
            world.setThunderDuration(0);
            world.setSpawnFlags(allowMonsters, allowAnimals);
            world.setSpawnLocation((int) spawnX, (int) spawnY, (int) spawnZ);

            msg.log("Created world " + name);
            return true;
        } else {
            msg.log("Failed to create world " + name);
            return false;
        }
    }

    public boolean deleteWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            msg.log("World " + worldName + " does not exist");
            return false;
        }

        if (Bukkit.getWorlds().size() == 1) {
            Bukkit.getLogger().warning("Cannot delete the last world");
            return false;
        }

        Bukkit.unloadWorld(world, true);

        boolean deleted = misc.deleteDirectory(world.getWorldFolder());

        if (deleted) {
            msg.log("Deleted world " + worldName);
        } else {
            msg.log("Failed to delete world " + worldName);
        }

        return deleted;
    }

}
