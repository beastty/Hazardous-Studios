package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CPunishment implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    public CPunishment(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.check")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /cpunishment <player>");
            sender.sendMessage("§7Check a player's current punishment status");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        // Get IP if available
        String ip = null;
        if (target != null && target.isOnline()) {
            ip = target.getAddress().getAddress().getHostAddress();
        } else {
            ip = databaseManager.getLastKnownIP(targetUUID);
        }

        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("§6§lPunishment Check: §e" + targetName);
        sender.sendMessage("§6§l" + "=".repeat(50));

        // Check ban status
        boolean isBanned = databaseManager.isBanned(targetUUID);
        if (isBanned) {
            DatabaseManager.BanInfo ban = databaseManager.getActiveBan(targetUUID);
            if (ban != null) {
                sender.sendMessage("");
                sender.sendMessage("§c§l⚠ BANNED");
                sender.sendMessage("§7Reason: §f" + ban.reason);
                sender.sendMessage("§7Banned by: §f" + ban.issuerName);
                sender.sendMessage("§7Date: §f" + dateFormat.format(new Date(ban.issuedAt)));
                if (ban.expiresAt == -1) {
                    sender.sendMessage("§7Duration: §cPermanent");
                } else {
                    long remaining = ban.expiresAt - System.currentTimeMillis();
                    sender.sendMessage("§7Time remaining: §f" + formatDuration(remaining));
                    sender.sendMessage("§7Expires: §f" + dateFormat.format(new Date(ban.expiresAt)));
                }
                sender.sendMessage("§7Ban ID: §f#" + ban.id);
            }
        } else {
            sender.sendMessage("§a✓ Not banned");
        }

        // Check mute status
        boolean isMuted = databaseManager.isMuted(targetUUID);
        if (isMuted) {
            DatabaseManager.MuteInfo mute = databaseManager.getActiveMute(targetUUID);
            if (mute != null) {
                sender.sendMessage("");
                sender.sendMessage("§e§l⚠ MUTED");
                sender.sendMessage("§7Reason: §f" + mute.reason);
                sender.sendMessage("§7Muted by: §f" + mute.issuerName);
                sender.sendMessage("§7Date: §f" + dateFormat.format(new Date(mute.issuedAt)));
                if (mute.expiresAt == -1) {
                    sender.sendMessage("§7Duration: §ePermanent");
                } else {
                    long remaining = mute.expiresAt - System.currentTimeMillis();
                    sender.sendMessage("§7Time remaining: §f" + formatDuration(remaining));
                    sender.sendMessage("§7Expires: §f" + dateFormat.format(new Date(mute.expiresAt)));
                }
                sender.sendMessage("§7Mute ID: §f#" + mute.id);
            }
        } else {
            sender.sendMessage("§a✓ Not muted");
        }

        // Check IP status
        List<DatabaseManager.PlayerIPRecord> alts = null;
        if (ip != null) {
            sender.sendMessage("");
            sender.sendMessage("§7IP Address: §f" + ip);

            // Check IP ban
            if (databaseManager.isIPBanned(ip)) {
                DatabaseManager.IPBanInfo ipBan = databaseManager.getIPBan(ip);
                if (ipBan != null) {
                    sender.sendMessage("§c§l⚠ IP IS BANNED");
                    sender.sendMessage("§7Reason: §f" + ipBan.reason);
                    sender.sendMessage("§7Banned by: §f" + ipBan.issuerName);
                }
            }

            // Check IP mute
            if (databaseManager.isIPMuted(ip)) {
                DatabaseManager.IPMuteInfo ipMute = databaseManager.getIPMute(ip);
                if (ipMute != null) {
                    sender.sendMessage("§e§l⚠ IP IS MUTED");
                    sender.sendMessage("§7Reason: §f" + ipMute.reason);
                    sender.sendMessage("§7Muted by: §f" + ipMute.issuerName);
                }
            }

            // Check for alts
            alts = databaseManager.getAccountsByIP(ip);
            if (alts.size() > 1) {
                sender.sendMessage("");
                sender.sendMessage("§6§lAlt Accounts Detected: §e" + (alts.size() - 1));
                int count = 0;
                for (DatabaseManager.PlayerIPRecord alt : alts) {
                    if (!alt.playerName.equalsIgnoreCase(targetName)) {
                        boolean altBanned = databaseManager.isBanned(alt.uuid);
                        boolean altMuted = databaseManager.isMuted(alt.uuid);
                        String status = "";
                        if (altBanned) status += " §c[BANNED]";
                        if (altMuted) status += " §e[MUTED]";
                        sender.sendMessage("§7  • §f" + alt.playerName + status);
                        count++;
                        if (count >= 5) {
                            sender.sendMessage("§7  ... and " + (alts.size() - 6) + " more");
                            break;
                        }
                    }
                }

                // Check for ban evasion
                boolean hasEvasion = false;
                for (DatabaseManager.PlayerIPRecord alt : alts) {
                    if (!alt.playerName.equalsIgnoreCase(targetName)) {
                        if (databaseManager.isBanned(alt.uuid)) {
                            hasEvasion = true;
                            sender.sendMessage("");
                            sender.sendMessage("§c§l⚠ POSSIBLE BAN EVASION DETECTED!");
                            sender.sendMessage("§7Alt account §c" + alt.playerName + " §7is currently banned");
                            sender.sendMessage("§7Consider using: §e/ipban " + targetName);
                            break;
                        }
                    }
                }
            }
        }

        // Warning count
        int warningCount = databaseManager.getWarningCount(targetUUID);
        sender.sendMessage("");
        sender.sendMessage("§7Total Warnings: §e" + warningCount);

        // Quick actions
        sender.sendMessage("");
        sender.sendMessage("§7Quick Actions:");
        sender.sendMessage("§e/punish " + targetName + " §7- Open punishment GUI");
        sender.sendMessage("§e/history " + targetName + " §7- View full history");
        if (ip != null && alts.size() > 1) {
            sender.sendMessage("§e/alts " + targetName + " §7- View detailed alt info");
        }

        sender.sendMessage("§6§l" + "=".repeat(50));

        return true;
    }

    private String formatDuration(long duration) {
        if (duration <= 0) return "Expired";

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