package dev.relismdev.rcore.worldManagement;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;

public class worldReader {

    public worldReader() {
        //
    }

    public JSONObject getLevel(int x, int z, World world) {
        int chunkX = x >> 4; // Divide by 16 to get the chunk X coordinate
        int chunkZ = z >> 4; // Divide by 16 to get the chunk Z coordinate
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);

        JSONObject levelData = new JSONObject();

        for (int blockX = chunkX * 16; blockX < (chunkX * 16) + 16; blockX++) {
            for (int blockZ = chunkZ * 16; blockZ < (chunkZ * 16) + 16; blockZ++) {
                Block block = world.getHighestBlockAt(blockX, blockZ);

                JSONObject blockData = new JSONObject();
                blockData.put("x", blockX);
                blockData.put("z", blockZ);
                blockData.put("block", block.getType().toString());

                levelData.put(blockX + "," + blockZ, blockData);
            }
        }

        return levelData;
    }
}
