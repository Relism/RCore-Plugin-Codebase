package dev.relismdev.rcore.proximityChat;

import dev.relismdev.rcore.utils.msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class voxelBuilder implements Listener {

    // Constants for enlargement and decreasal rates
    private static final double minimumEnlargementRate = 0.05;
    private static final double enlargementDecreasal = 0.02;
    private static final double basePerPointSize = 32; // Adjust this value as needed

    // Inner class representing a voxel
    public static class Voxel {
        List<Player> populator;  // List of players inside the voxel
        double[][] vertices;  // Coordinates of the voxel vertices
        double[] midpoint;  // Center of gravity coordinate point
        double size;  // Size of the voxel
        World world;

        // Constructor to create a voxel from two players
        Voxel(Player player1, Player player2) {
            this.populator = new ArrayList<>();
            this.populator.add(player1);
            this.populator.add(player2);

            // Calculate the midpoint (center of gravity) of the voxel
            this.midpoint = calculateCenterOfGravity(populator);

            double enlargementRate = calculateEnlargementRate(populator.size());
            double voxelSize = calculateVoxelSize(populator.size(), enlargementRate);
            this.vertices = calculateVertices(midpoint, voxelSize);
            this.world = player1.getWorld();

            // Add the voxel to the list of voxels
            voxels.add(this);
        }
    }

    private void placeBlocksInVertices(Voxel voxel) {
        for (double[] vertex : voxel.vertices) {
            int x = (int) vertex[1];
            int y = (int) vertex[2];
            int z = (int) vertex[3];
            msg.log(x + " " + y + " " + z);
            msg.log(String.valueOf(voxel.midpoint[0] + " " + voxel.midpoint[1] + " " + voxel.midpoint[2]));
            Block block = voxel.world.getBlockAt(x, y, z);

            block.setType(Material.STONE);
        }
    }

    // List to store all created voxels
    private static List<Voxel> voxels = new ArrayList<>();

    // Get the list of all created voxels
    public List<Voxel> getVoxels() {
        return voxels;
    }

    // Event handler for player movement
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player movingPlayer = event.getPlayer();
        Location previousLocation = event.getFrom();
        Location currentLocation = event.getTo();

        // Check if there's a change in the X, Y, or Z coordinates
        if (hasPlayerMoved(previousLocation, currentLocation)) {
            // Check nearby players and create new voxels if needed
            for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
                msg.log(targetPlayer.getName());
                if (targetPlayer != movingPlayer && movingPlayer.getLocation().distance(targetPlayer.getLocation()) <= 32) {
                    boolean foundVoxel = false;
                    // Check if there's already a voxel containing both players
                    for (Voxel voxel : voxels) {
                        if (voxel.populator.contains(movingPlayer) && voxel.populator.contains(targetPlayer)) {
                            msg.log("This voxel already exists");
                            foundVoxel = true;
                            break;
                        }
                    }
                    // If no existing voxel is found, create a new voxel
                    if (!foundVoxel) {
                        Voxel newVoxel = new Voxel(movingPlayer, targetPlayer);
                        msg.log("New voxel created by " + movingPlayer.getName() + " and " + targetPlayer.getName());
                        break;  // Stop searching once a new voxel is created
                    }
                }
            }

            // Update existing voxels based on player movement
            for (Voxel voxel : voxels) {
                boolean movedInsideVoxel = isInsideVoxel(movingPlayer, voxel);

                // If player moved inside the voxel and isn't already in its populator list, add the player
                if (movedInsideVoxel && !voxel.populator.contains(movingPlayer)) {
                    updateVoxel(movingPlayer, voxel, true);
                    placeBlocksInVertices(voxel);
                    msg.log(movingPlayer.getName() + " moved inside voxel");
                }
                // If player moved outside the voxel and was previously in its populator list, remove the player
                else if (!movedInsideVoxel && voxel.populator.contains(movingPlayer)) {
                    updateVoxel(movingPlayer, voxel, false);
                    placeBlocksInVertices(voxel);
                    msg.log(movingPlayer.getName() + " moved outside voxel");
                }

                // If the player is in the voxel's populator list, recalculate the voxel's midpoint
                if (voxel.populator.contains(movingPlayer)) {
                    voxel.midpoint = calculateCenterOfGravity(voxel.populator);
                }
            }
        }
    }

    private boolean hasPlayerMoved(Location from, Location to) {
        return from.getX() != to.getX()
                || from.getY() != to.getY()
                || from.getZ() != to.getZ();
    }

    private void updateVoxel(Player player, Voxel voxel, Boolean increase){
        if(increase){
            voxel.populator.add(player);
        } else {
            voxel.populator.remove(player);
        }
        Integer count = voxel.populator.size();
        voxel.midpoint = calculateCenterOfGravity(voxel.populator);
        double enlargementRate = calculateEnlargementRate(count);
        voxel.size = calculateVoxelSize(count, enlargementRate);
        voxel.vertices = calculateVertices(voxel.midpoint, voxel.size);
    }

    // Check if a player is inside a voxel
    private boolean isInsideVoxel(Player player, Voxel voxel) {
        double[][] vertices = voxel.vertices;
        double playerX = player.getLocation().getX();
        double playerY = player.getLocation().getY();
        double playerZ = player.getLocation().getZ();

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;

        for (double[] vertex : vertices) {
            double x = vertex[1];
            double y = vertex[2];
            double z = vertex[3];

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);  // Corrected this line
            maxY = Math.max(maxY, y);  // Corrected this line
            maxZ = Math.max(maxZ, z);  // Corrected this line
        }

        return playerX >= minX && playerX <= maxX &&
                playerY >= minY && playerY <= maxY &&
                playerZ >= minZ && playerZ <= maxZ;
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

    private static double calculateEnlargementRate(int number) {
        return Math.max(1 - enlargementDecreasal * (number - 1), minimumEnlargementRate);
    }

    private static double calculateVoxelSize(int number, double enlargementRate) {
        double basePerPointSize = 1.0; // Replace with your actual value
        return roundToTwoDecimals(number * basePerPointSize * enlargementRate);
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