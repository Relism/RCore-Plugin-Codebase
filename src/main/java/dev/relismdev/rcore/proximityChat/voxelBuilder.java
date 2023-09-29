package dev.relismdev.rcore.proximityChat;

import dev.relismdev.rcore.storage.localStorage;
import dev.relismdev.rcore.storage.playerStorage;
import dev.relismdev.rcore.utils.msg;
import dev.relismdev.rcore.utils.misc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class voxelBuilder implements Listener {

    // Constants for enlargement and decreasal rates
    private static final double minimumEnlargementRate = 0.05;
    private static final double enlargementDecreasal = 0.02;
    private static final double basePerPointSize = 32; // Adjust this value as needed

    private static misc misc = new misc();
    private static playerStorage ps = new playerStorage();
    private static localStorage ls = new localStorage();

    // Inner class representing a voxel
    public static class Voxel {
        String id;
        List<Player> populator;  // List of players inside the voxel
        double[][] vertices;  // Coordinates of the voxel vertices
        double[] midpoint;  // Center of gravity coordinate point
        double size;  // Size of the voxel
        World world;
        Map<Location, Block> floatingBlocks;  // Map of floating blocks in the voxel

        // Constructor to create a voxel from two players
        Voxel(Player player1, Player player2) {
            populator = new ArrayList<>();
            populator.add(player1);
            populator.add(player2);
            id = misc.randomStr(8);

            // Calculate the midpoint (center of gravity) of the voxel
            midpoint = calculateCenterOfGravity(populator);

            double enlargementRate = calculateEnlargementRate(populator.size());
            double voxelSize = calculateSize(populator.size(), enlargementRate);
            vertices = calculateVertices(midpoint, voxelSize);
            world = player1.getWorld();

            // Initialize the floatingBlocks map
            floatingBlocks = new HashMap<>();

            // Add the voxel to the list of voxels
            voxels.add(this);
            ps.set(player1, "currentVoxel", id);
            ps.set(player2, "currentVoxel", id);
            msg.log(id + " has been created");
        }

        void populate(Boolean populate, Player player){
            if(populate){
                populator.add(player);
                msg.log(player.getName() + " has joined the voxel");
                ps.set(player, "currentVoxel", id);
            } else {
                populator.remove(player);
                msg.log(player.getName() + " has left the voxel");
                ps.set(player, "currentVoxel", null);
            }
            if(populator.size() <= 1){
                disband();
            } else {
                msg.log("size : " + size);
                update();
            }
        }

        void update(){
            Integer count = populator.size();
            midpoint = calculateCenterOfGravity(populator);
            double enlargementRate = calculateEnlargementRate(count);
            size = calculateSize(count, enlargementRate);
            vertices = calculateVertices(midpoint, size);
        }

        void disband(){
            voxels.remove(this);
            clearFloatingBlocks();
            msg.log(id + " has been disbanded");
        }

        void showVertices() {
            clearFloatingBlocks();
            for (double[] vertex : vertices) {
                int x = (int) vertex[1];
                int y = (int) vertex[2];
                int z = (int) vertex[3];

                Location location = new Location(world, x, y, z);
                Block block = world.getBlockAt(location);

                // Set the block type to your desired floating block type
                block.setType(Material.GLASS);
                floatingBlocks.put(location, block);
            }
        }

        void clearFloatingBlocks() {
            for (Block block : floatingBlocks.values()) {
                block.setType(Material.AIR);
            }
            floatingBlocks.clear();
        }

        boolean moved(Player player) {
            double playerX = player.getLocation().getX();
            double playerY = player.getLocation().getY();
            double playerZ = player.getLocation().getZ();

            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;

            boolean moved = false;

            for (double[] vertex : vertices) {
                double x = vertex[1];
                double y = vertex[2];
                double z = vertex[3];

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                minZ = Math.min(minZ, z);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
                maxZ = Math.max(maxZ, z);
            }

            if (playerX >= minX && playerX <= maxX &&
                    playerY >= minY && playerY <= maxY &&
                    playerZ >= minZ && playerZ <= maxZ) {
                    moved = true;
            }

            return moved;
        }

        private static double calculateSize(int number, double enlargementRate) {
            return roundToTwoDecimals(number * basePerPointSize * enlargementRate);
        }

        private static double calculateEnlargementRate(int number) {
            return Math.max(1 - enlargementDecreasal * (number - 1), minimumEnlargementRate);
        }

    }

    // List to store all created voxels
    private static List<Voxel> voxels = new ArrayList<>();

    // Get the list of all created voxels
    public List<Voxel> getVoxels() {
        return voxels;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player leavingPlayer = event.getPlayer();
        if(inAVoxel(leavingPlayer) != null){
            Voxel voxel = playerVoxel(leavingPlayer);
            voxel.populate(false, leavingPlayer);
        }
    }

    // Event handler for player movement
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaY = event.getTo().getY() - event.getFrom().getY();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();

        if (deltaX != 0 || deltaY != 0 || deltaZ != 0) {
            handleMove(player);
        }
    }

    private void handleMove(Player player) {
        Voxel currentVoxel = inAVoxel(player);
        Voxel movedVoxel = movedInVoxel(player);

        if (currentVoxel != null) {
            msg.log("Current : " + currentVoxel.id);
            //is in a voxel
            //misc.printMatrix(currentVoxel.vertices);
            if (movedVoxel == currentVoxel) {
                msg.log("Moved : " + movedVoxel.id);
                //moved in the populated voxel
                if (currentVoxel.moved(player)){
                    //msg.log("Player moved within the voxel");
                    currentVoxel.update();
                    currentVoxel.showVertices();
                } else {
                    //msg.log("Player moved out of a voxel");
                    currentVoxel.populate(false, player);
                    movedVoxel.populate(false, player);
                }
            } else {
                //msg.log("Player moved from one voxel to another");
                currentVoxel.populate(false, player);
                movedVoxel.populate(true, player);
            }
        } else {
            if (movedVoxel != null) {
                //msg.log("Player moved in a voxel");
                movedVoxel.populate(true, player);
            } else {
                //msg.log("Player is not in a voxel");
                Location playerLocation = player.getLocation();
                for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
                    if (player != nearbyPlayer) {
                        Location nearbyPlayerLocation = nearbyPlayer.getLocation();
                        double distance = playerLocation.distance(nearbyPlayerLocation);
                        if (distance <= 32) {
                            new Voxel(player, nearbyPlayer);
                        }
                    }
                }
            }
        }
    }


    private Voxel inAVoxel(Player player) {
        Voxel foundVoxel = null;

        for (Voxel voxel : voxels) {
            if (voxel.populator.contains(player)) {
                foundVoxel = voxel;
                break;
            }
        }

        return foundVoxel;
    }

    private Voxel playerVoxel(Player player){
        Voxel pVoxel = null;
        for(Voxel voxel : voxels){
            if(voxel.populator.contains(player)){
                pVoxel = voxel;
            }
        }
        return pVoxel;
    }

    private Voxel movedInVoxel(Player player) {
        double playerX = player.getLocation().getX();
        double playerY = player.getLocation().getY();
        double playerZ = player.getLocation().getZ();

        for (Voxel voxel : voxels) {
            double[][] vertices = voxel.vertices;

            // Check if player coordinates are inside voxel cube vertices
            boolean insideVoxel = false;
            for (double[] vertex : vertices) {
                double x = vertex[0];
                double y = vertex[1];
                double z = vertex[2];

                if (playerX < x || playerY < y || playerZ < z ||
                        playerX > x + 1 || playerY > y + 1 || playerZ > z + 1) {
                    insideVoxel = true;
                    break;
                }
            }

            if (insideVoxel) {
                return voxel;
            }
        }

        return null; // Player coordinates are not inside any voxel
    }

    // Calculate the center of gravity of players in a voxel
    private static double[] calculateCenterOfGravity(List<Player> populator) {
        if (populator.isEmpty()) {
            throw new IllegalStateException("Populator must not be empty.");
        }

        double sumX = 0, sumY = 0, sumZ = 0;

        for (Player player : populator) {
            double[] playerCoords = new double[]{
                    roundToTwoDecimals(player.getLocation().getX()),
                    roundToTwoDecimals(player.getLocation().getY()),
                    roundToTwoDecimals(player.getLocation().getZ())
            };

            sumX += playerCoords[0];
            sumY += playerCoords[1];
            sumZ += playerCoords[2];
        }

        double centerX = sumX / populator.size();
        double centerY = sumY / populator.size();
        double centerZ = sumZ / populator.size();

        return new double[]{centerX, centerY, centerZ};
    }


    // Round a coordinate to two decimal points
    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // Calculate the vertices of the voxel
    private static double[][] calculateVertices(double[] midpoint, double size) {
        double halfSize = size / 2;
        return new double[][]{
                {0, roundToTwoDecimals(midpoint[0] - halfSize), roundToTwoDecimals(midpoint[1] - halfSize), roundToTwoDecimals(midpoint[2] - halfSize)},
                {1, roundToTwoDecimals(midpoint[0] + halfSize), roundToTwoDecimals(midpoint[1] - halfSize), roundToTwoDecimals(midpoint[2] - halfSize)},
                {2, roundToTwoDecimals(midpoint[0] - halfSize), roundToTwoDecimals(midpoint[1] - halfSize), roundToTwoDecimals(midpoint[2] + halfSize)},
                {3, roundToTwoDecimals(midpoint[0] + halfSize), roundToTwoDecimals(midpoint[1] - halfSize), roundToTwoDecimals(midpoint[2] + halfSize)},
                {4, roundToTwoDecimals(midpoint[0] - halfSize), roundToTwoDecimals(midpoint[1] + halfSize), roundToTwoDecimals(midpoint[2] - halfSize)},
                {5, roundToTwoDecimals(midpoint[0] + halfSize), roundToTwoDecimals(midpoint[1] + halfSize), roundToTwoDecimals(midpoint[2] - halfSize)},
                {6, roundToTwoDecimals(midpoint[0] - halfSize), roundToTwoDecimals(midpoint[1] + halfSize), roundToTwoDecimals(midpoint[2] + halfSize)},
                {7, roundToTwoDecimals(midpoint[0] + halfSize), roundToTwoDecimals(midpoint[1] + halfSize), roundToTwoDecimals(midpoint[2] + halfSize)}
        };
    }
}