package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class IPMute implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public IPMute(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.ipmute")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /ipmute <player|ip> [duration] [reason] [-s]");
            sender.sendMessage("§7Mute an IP address or all accounts on a player's IP");
            return true;
        }

        String target = args[0];
        String ip = null;

        // Check if it's an IP address or player name
        if (target.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            ip = target;
        } else {
            Player player = Bukkit.getPlayer(target);
            if (player != null && player.isOnline()) {
                ip = player.getAddress().getAddress().getHostAddress();
            } else {
                UUID uuid = Bukkit.getOfflinePlayer(target).getUniqueId();
                ip = databaseManager.getLastKnownIP(uuid);
            }
        }

        if (ip == null) {
            sender.sendMessage("§cCould not find IP address for " + target);
            return true;
        }

        if (databaseManager.isIPMuted(ip)) {
            sender.sendMessage("§cThis IP address is already muted!");
            return true;
        }

        boolean silent = false;
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

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = reasonStartIndex; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-s")) {
                silent = true;
            } else {
                reasonBuilder.append(args[i]).append(" ");
            }
        }
        String reason = reasonBuilder.toString().trim();
        if (reason.isEmpty()) {
            reason = "IP Muted by an operator";
        }

        long expiresAt = duration == -1 ? -1 : System.currentTimeMillis() + duration;
        String issuer = sender instanceof Player ? sender.getName() : "Console";
        UUID issuerUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        databaseManager.addIPMute(ip, issuerUUID, issuer, reason, expiresAt);

        // Notify all players with this IP
        int notified = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            String playerIP = online.getAddress().getAddress().getHostAddress();
            if (playerIP.equals(ip)) {
                online.sendMessage("§c§lYou have been IP-muted!");
                online.sendMessage("§7Reason: §f" + reason);
                notified++;
            }
        }

        String durationStr = duration == -1 ? "permanently" : formatDuration(duration);
        String notification = "§e" + ip + " §7has been IP-muted by §e" + issuer + " §7for §e" + durationStr + " §7- §f" + reason;
        if (notified > 0) {
            notification += " §7(" + notified + " player(s) affected)";
        }

        if (silent) {
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("duckypunishment.notify")) {
                    staff.sendMessage("§7[SILENT] " + notification);
                }
            }
            sender.sendMessage("§7[SILENT] " + notification);
        } else {
            Bukkit.broadcast(notification, "duckypunishment.notify");
            sender.sendMessage(notification);
        }

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
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + " day" + (days > 1 ? "s" : "");
        if (hours > 0) return hours + " hour" + (hours > 1 ? "s" : "");
        if (minutes > 0) return minutes + " minute" + (minutes > 1 ? "s" : "");
        return seconds + " second" + (seconds > 1 ? "s" : "");
    }
}

