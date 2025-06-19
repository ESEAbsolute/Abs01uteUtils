package com.eseabsolute.abs01uteUtils.commands;

import com.eseabsolute.abs01uteUtils.Abs01uteUtils;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class TeleportToCursorCommand implements CommandExecutor {
    private static final double MAX_DISTANCE = 256;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        Player player;
        double distance;
        boolean acceptAir = false;
        if (sender instanceof ConsoleCommandSender) {
            // console logic: /teleporttocursor <Player> <Distance> <AcceptAir>

            if (args.length != 3) {
                sender.sendMessage("Syntax invalid: (by CONSOLE) /teleporttocursor <Player> <Distance> <AcceptAir>");
                return true;
            }

            player = Bukkit.getPlayerExact(args[0]);
            if (player == null || !player.isOnline()) {
                sender.sendMessage("Cannot find online player " + args[0]);
                return true;
            }

            try {
                distance = Math.min(Integer.parseInt(args[1]), (int) MAX_DISTANCE);
                if (distance < 0) {
                    sender.sendMessage("Distance should not be negative.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("Distance should be a number.");
                return true;
            }

            if (args[2].equalsIgnoreCase("true")) {
                acceptAir = true;
            } else if (!args[2].equalsIgnoreCase("false")) {
                sender.sendMessage("AcceptAir must be true or false.");
                return true;
            }

            teleportPlayerToCursorImpl(player, distance, acceptAir);
        } else if (sender instanceof Player) {
            // Player logic: /teleporttocursor <Player> <Distance> <AcceptAir>

            if (args.length != 2) {
                sender.sendMessage("Syntax invalid: (by player) /teleporttocursor <Distance> <AcceptAir>");
                return true;
            }

            try {
                distance = Math.min(Integer.parseInt(args[0]), (int) MAX_DISTANCE);
                if (distance < 0) {
                    sender.sendMessage("Distance should not be negative.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("Distance should be a number.");
                return true;
            }

            player = (Player) sender;

            if (args[1].equalsIgnoreCase("true")) {
                acceptAir = true;
            } else if (!args[1].equalsIgnoreCase("false")) {
                sender.sendMessage("AcceptAir must be true or false.");
                return true;
            }

            teleportPlayerToCursorImpl(player, distance, acceptAir);
        } else {
            sender.sendMessage("Unsupported command sender.");
        }

        return true;
    }

    private void teleportPlayerToCursorImpl(Player player, double distance, boolean acceptAir) {
        player.getScheduler().run(Abs01uteUtils.getInstance(), scheduledTask -> {
            RayTraceResult result = player.getWorld().rayTrace(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    distance,
                    FluidCollisionMode.NEVER,
                    true,
                    0.1,
                    entity -> false
            );
//            RayTraceResult result = player.rayTraceBlocks(distance, FluidCollisionMode.NEVER);
            Location eyeLoc = player.getEyeLocation();
            Location targetLoc;

            if (result == null || result.getHitBlock() == null) {
                if (acceptAir) {
                    Vector direction = eyeLoc.getDirection().normalize();
                    targetLoc = eyeLoc.add(direction.multiply(distance));
                } else {
                    player.sendMessage("No block in sight within range.");
                    return;
                }
            } else {
                Block targetBlock = result.getHitBlock();
                targetLoc = targetBlock.getLocation().add(0.5, 0.0, 0.5);
                BlockFace face = result.getHitBlockFace();
                if (face != null) {
                    switch (face) {
                        case DOWN -> targetLoc = targetLoc.add(0, -1.0, 0);
                        case NORTH -> targetLoc = targetLoc.add(0, 0, -1.0);
                        case SOUTH -> targetLoc = targetLoc.add(0, 0, 1.0);
                        case WEST -> targetLoc = targetLoc.add(-1.0, 0, 0);
                        case EAST -> targetLoc = targetLoc.add(1.0, 0, 0);
                        default -> targetLoc = targetLoc.add(0, 1.0, 0);
                    }
                } else {
                    player.sendMessage("Block face is null.");
                    return;
                }
            }
            targetLoc.setPitch(eyeLoc.getPitch());
            targetLoc.setYaw(eyeLoc.getYaw());

            Location finalTargetLoc = targetLoc;
            player.teleportAsync(finalTargetLoc).thenAccept(success -> {
                player.setFallDistance(0);
//                if (success) {
//                    player.sendMessage("Teleported to " +
//                            finalTargetLoc.getBlockX() + ", " +
//                            finalTargetLoc.getBlockY() + ", " +
//                            finalTargetLoc.getBlockZ());
//                } else {
//                    player.sendMessage("Teleportation failed.");
//                }
            });
        }, null);
    }
}
