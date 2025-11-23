package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.auth.duckyPunishment.managers.DatabaseManager.PunishmentHistory;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class punishmentHistory implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    public punishmentHistory(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.history")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /history <player>");
            return true;
        }

        String targetName = args[0];
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        PunishmentHistory history = databaseManager.getHistory(targetUUID);

        if (history.getTotalPunishments() == 0) {
            sender.sendMessage("§e" + targetName + " has no punishment history.");
            return true;
        }

        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("§6§lPunishment History for " + targetName);
        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("");

        // Display Bans
        if (!history.getBans().isEmpty()) {
            sender.sendMessage("§c§lBans (" + history.getBans().size() + "):");
            for (DatabaseManager.BanInfo ban : history.getBans()) {
                sender.sendMessage("§7  • §fIssued by: §e" + ban.issuerName);
                sender.sendMessage("§7    Reason: §f" + ban.reason);
                sender.sendMessage("§7    Date: §f" + dateFormat.format(new Date(ban.issuedAt)));
                if (ban.expiresAt == -1) {
                    sender.sendMessage("§7    Duration: §cPermanent");
                } else {
                    sender.sendMessage("§7    Duration: §f" + formatDuration(ban.expiresAt - ban.issuedAt));
                }
                sender.sendMessage("");
            }
        }

        // Display Mutes
        if (!history.getMutes().isEmpty()) {
            sender.sendMessage("§e§lMutes (" + history.getMutes().size() + "):");
            for (DatabaseManager.MuteInfo mute : history.getMutes()) {
                sender.sendMessage("§7  • §fIssued by: §e" + mute.issuerName);
                sender.sendMessage("§7    Reason: §f" + mute.reason);
                sender.sendMessage("§7    Date: §f" + dateFormat.format(new Date(mute.issuedAt)));
                if (mute.expiresAt == -1) {
                    sender.sendMessage("§7    Duration: §cPermanent");
                } else {
                    sender.sendMessage("§7    Duration: §f" + formatDuration(mute.expiresAt - mute.issuedAt));
                }
                sender.sendMessage("");
            }
        }

        // Display Kicks
        if (!history.getKicks().isEmpty()) {
            sender.sendMessage("§6§lKicks (" + history.getKicks().size() + "):");
            for (DatabaseManager.KickInfo kick : history.getKicks()) {
                sender.sendMessage("§7  • §fIssued by: §e" + kick.issuerName);
                sender.sendMessage("§7    Reason: §f" + kick.reason);
                sender.sendMessage("§7    Date: §f" + dateFormat.format(new Date(kick.issuedAt)));
                sender.sendMessage("");
            }
        }

        // Display Warnings
        if (!history.getWarnings().isEmpty()) {
            sender.sendMessage("§d§lWarnings (" + history.getWarnings().size() + "):");
            for (DatabaseManager.WarningInfo warning : history.getWarnings()) {
                sender.sendMessage("§7  • §fIssued by: §e" + warning.issuerName);
                sender.sendMessage("§7    Reason: §f" + warning.reason);
                sender.sendMessage("§7    Date: §f" + dateFormat.format(new Date(warning.issuedAt)));
                sender.sendMessage("");
            }
        }

        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("§7Total Punishments: §e" + history.getTotalPunishments());
        sender.sendMessage("§6§l" + "=".repeat(50));

        return true;
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
}