package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Warn implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public Warn(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.warn")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /warn <player> [reason] [-s]");
            sender.sendMessage("§7Add -s for silent warning (staff only)");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID;

        if (target != null) {
            targetUUID = target.getUniqueId();
        } else {
            targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        }

        // Check if trying to punish self
        if (sender instanceof Player && ((Player) sender).getUniqueId().equals(targetUUID)) {
            sender.sendMessage("§cYou cannot warn yourself!");
            return true;
        }

        // Check hierarchy
        if (sender instanceof Player && target != null) {
            if (!configManager.canPunish((Player) sender, target)) {
                sender.sendMessage("§cYou cannot warn this player! They have a higher rank than you.");
                return true;
            }
        }

        // Check for silent flag
        boolean silent = false;
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-s")) {
                silent = true;
            } else {
                reasonBuilder.append(args[i]).append(" ");
            }
        }

        String reason = reasonBuilder.toString().trim();
        if (reason.isEmpty()) {
            reason = configManager.getMessage("default-warn-reason");
        }

        String issuer = sender instanceof Player ? sender.getName() : "Console";
        UUID issuerUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        // Add warning to database
        int warningId = databaseManager.addWarning(targetUUID, targetName, issuerUUID, issuer, reason);

        // Notify target if online
        if (target != null && target.isOnline()) {
            target.sendMessage("§c§l⚠ WARNING ⚠");
            target.sendMessage("§7You have been warned by §c" + issuer);
            target.sendMessage("§7Reason: §f" + reason);
            target.sendMessage("§7Total warnings: §e" + databaseManager.getWarningCount(targetUUID));
        }

        // Notify staff
        String notification = configManager.getMessage("warn-notification")
                .replace("%player%", targetName)
                .replace("%issuer%", issuer)
                .replace("%reason%", reason)
                .replace("%id%", String.valueOf(warningId));

        if (silent) {
            // Silent mode - only staff see it
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("duckypunishment.notify")) {
                    staff.sendMessage("§7[SILENT] " + notification);
                }
            }
            sender.sendMessage("§7[SILENT] " + notification);
        } else {
            // Normal broadcast
            Bukkit.broadcast(notification, "duckypunishment.notify");
            sender.sendMessage(notification);
        }

        return true;
    }
}