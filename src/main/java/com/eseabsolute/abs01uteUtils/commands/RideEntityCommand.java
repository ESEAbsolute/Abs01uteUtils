package com.eseabsolute.abs01uteUtils.commands;

import com.eseabsolute.abs01uteUtils.Abs01uteUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RideEntityCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length < 2) {
            sender.sendMessage("Syntax invalid: /rideentity mount <vehicle> <rider> [executePlayer] or /rideentity dismount <vehicle> [executePlayer]");
        }
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        if (args[0].equals("mount")) {
            rideEntityMountImpl(sender, newArgs);
        } else if (args[0].equals("dismount")) {
            rideEntityDismountImpl(sender, newArgs);
        } else {
            sender.sendMessage("Syntax invalid: /rideentity mount <vehicle> <rider> [executePlayer] or /rideentity dismount <vehicle> [executePlayer]");
        }
        return true;
    }

    private void rideEntityDismountImpl(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player;
        Entity vehicle;
        if (sender instanceof ConsoleCommandSender) {
            if (args.length == 1) {
                vehicle = parseEntityForceUUID(args[0]);
                if (vehicle == null) {
                    sender.sendMessage("Vehicle not an valid UUID! Fix the UUID issue or add the 2nd argument <executePlayer>");
                    return;
                }
            } else if (args.length == 2) {
                player = Bukkit.getPlayerExact(args[1]);
                if (player == null || !player.isOnline()) {
                    sender.sendMessage("Cannot find online player " + args[0]);
                    return;
                }
                vehicle = parseEntity(args[0], player);
            } else {
                sender.sendMessage("Syntax invalid: /rideentity dismount <vehicle> [executePlayer]");
                return;
            }
        } else if (sender instanceof Player) {
            if (args.length != 1) {
                sender.sendMessage("Syntax invalid: /rideentity dismount <vehicle>");
                return;
            }
            vehicle = parseEntity(args[0], sender);
        } else {
            sender.sendMessage("Unsupported command sender.");
            return;
        }

        if (vehicle == null) {
            sender.sendMessage("Vehicle not found!");
            return;
        }

        vehicle.getScheduler().run(Abs01uteUtils.getInstance(), task -> {
            List<Entity> passengers = vehicle.getPassengers();
            if (passengers.isEmpty()) {
                return;
            }
            for (Entity passenger : passengers) {
                vehicle.removePassenger(passenger);
            }
        }, null);
    }

    private void rideEntityMountImpl(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player;
        Entity vehicle;

        if (sender instanceof ConsoleCommandSender) {
            if (args.length == 2) {
                vehicle = parseEntityForceUUID(args[0]);
                if (vehicle == null) {
                    sender.sendMessage("Vehicle not an valid UUID! Fix the UUID issue or add the 3rd argument <executePlayer>");
                    return;
                }
            } else if (args.length == 3) {
                player = Bukkit.getPlayerExact(args[2]);
                if (player == null || !player.isOnline()) {
                    sender.sendMessage("Cannot find online player " + args[0]);
                    return;
                }
                vehicle = parseEntity(args[0], player);
            } else {
                sender.sendMessage("Syntax invalid: /rideentity mount <vehicle> <rider> [executePlayer]");
                return;
            }
        } else if (sender instanceof Player) {
            if (args.length != 2) {
                sender.sendMessage("Syntax invalid: /rideentity mount <vehicle> <rider>");
                return;
            }
            vehicle = parseEntity(args[0], sender);
        } else {
            sender.sendMessage("Unsupported command sender.");
            return;
        }

        Entity rider = parseEntity(args[1], vehicle);

        if (vehicle == null || rider == null) {
            sender.sendMessage("Vehicle or rider not found!");
            return;
        }

        Entity finalVehicle = vehicle;
        vehicle.getScheduler().run(Abs01uteUtils.getInstance(), task -> {
            Location vehicleLocation = finalVehicle.getLocation();
            rider.getScheduler().run(Abs01uteUtils.getInstance(), task2 -> {
                Location riderLocation = rider.getLocation();
                if (riderLocation.getWorld() != vehicleLocation.getWorld() ||
                        riderLocation.getBlockX() >> 4 != vehicleLocation.getBlockX() >> 4 ||
                        riderLocation.getBlockZ() >> 4 != vehicleLocation.getBlockZ() >> 4) {
                    sender.sendMessage("Two entities are not in the same chunk, cannot ride!");
                    return;
                }
                rider.getScheduler().run(Abs01uteUtils.getInstance(), task3 -> finalVehicle.addPassenger(rider), null);
            }, null);
        }, null);
    }

    private Entity parseEntityForceUUID(String arg) {
        try {
            UUID uuid = UUID.fromString(arg);
            for (World world : Bukkit.getWorlds()) {
                Entity entity = world.getEntity(uuid);
                if (entity != null) {
                    return entity;
                }
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    private Entity parseEntity(String arg, CommandSender sender) {
        try {
            UUID uuid = UUID.fromString(arg);
            for (World world : Bukkit.getWorlds()) {
                Entity entity = world.getEntity(uuid);
                if (entity != null) {
                    return entity;
                }
            }
        } catch (IllegalArgumentException ignored) {
        }

        List<Entity> entities = Bukkit.selectEntities(sender, arg);
        if (!entities.isEmpty()) {
            return entities.getFirst();
        }

        return null;
    }
}
