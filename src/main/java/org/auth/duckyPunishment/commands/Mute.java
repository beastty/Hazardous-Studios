package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Mute implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public Mute(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.mute")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /mute <player> [duration] [reason]");
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

        // Check if already muted
        if (databaseManager.isMuted(targetUUID)) {
            sender.sendMessage("§c" + targetName + " is already muted!");
            return true;
        }

        // Parse duration
        long duration = -1;
        int reasonStartIndex = 1;

        if (args.length >= 2) {
            duration = parseDuration(args[1]);
            if (duration != -2) {
                reasonStartIndex = 2;
            } else {
                duration = -1;
                reasonStartIndex = 1;
            }
        }

        // Parse reason
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = reasonStartIndex; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();
        if (reason.isEmpty()) {
            reason = configManager.getMessage("default-mute-reason");
        }

        long expiresAt = duration == -1 ? -1 : System.currentTimeMillis() + duration;
        String issuer = sender instanceof Player ? sender.getName() : "Console";
        UUID issuerUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        databaseManager.addMute(targetUUID, targetName, issuerUUID, issuer, reason, expiresAt);

        // Notify muted player
        if (target != null && target.isOnline()) {
            String muteMessage = formatMuteMessage(targetName, reason, duration, expiresAt);
            target.sendMessage(muteMessage);
        }

        // Notify staff
        String durationStr = duration == -1 ? "permanently" : formatDuration(duration);
        String notification = configManager.getMessage("mute-notification")
                .replace("%player%", targetName)
                .replace("%issuer%", issuer)
                .replace("%duration%", durationStr)
                .replace("%reason%", reason);

        Bukkit.broadcast(notification, "duckypunishment.notify");
        sender.sendMessage(notification);

        return true;
    }

    private long parseDuration(String input) {
        if (input.equalsIgnoreCase("perm") || input.equalsIgnoreCase("permanent")) {
            return -1;
        }

        try {
            char unit = input.charAt(input.length() - 1);
            int value = Integer.parseInt(input.substring(0, input.length() - 1));

            switch (Character.toLowerCase(unit)) {
                case 's': return value * 1000L;
                case 'm': return value * 60000L;
                case 'h': return value * 3600000L;
                case 'd': return value * 86400000L;
                case 'w': return value * 604800000L;
                case 'y': return value * 31536000000L;
                default: return -2;
            }
        } catch (Exception e) {
            return -2;
        }
    }

    private String formatDuration(long duration) {
        if (duration == -1) return "Permanent";

        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + " day" + (days > 1 ? "s" : "");
        if (hours > 0) return hours + " hour" + (hours > 1 ? "s" : "");
        if (minutes > 0) return minutes + " minute" + (minutes > 1 ? "s" : "");
        return seconds + " second" + (seconds > 1 ? "s" : "");
    }

    private String formatMuteMessage(String player, String reason, long duration, long expiresAt) {
        StringBuilder msg = new StringBuilder();
        msg.append("§c§lYou have been muted!\n\n");
        msg.append("§7Player: §f").append(player).append("\n");
        msg.append("§7Reason: §f").append(reason).append("\n");

        if (duration == -1) {
            msg.append("§7Duration: §cPermanent\n");
        } else {
            msg.append("§7Duration: §f").append(formatDuration(duration)).append("\n");
            msg.append("§7Expires: §f").append(new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm").format(new java.util.Date(expiresAt))).append("\n");
        }

        return msg.toString();
    }
}