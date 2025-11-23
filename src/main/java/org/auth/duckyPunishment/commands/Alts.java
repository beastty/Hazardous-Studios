package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class Alts implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public Alts(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.alts")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /alts <player>");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        String ip = null;

        // Get IP address
        if (target != null && target.isOnline()) {
            ip = target.getAddress().getAddress().getHostAddress();
        } else {
            // Try to get from database
            ip = databaseManager.getLastKnownIP(targetUUID);
        }

        if (ip == null) {
            sender.sendMessage("§cCould not find IP address for " + targetName);
            sender.sendMessage("§7Player must join at least once for IP tracking.");
            return true;
        }

        // Get all accounts with this IP
        List<DatabaseManager.PlayerIPRecord> alts = databaseManager.getAccountsByIP(ip);

        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("§6§lAlt Account Check: §e" + targetName);
        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("§7IP Address: §f" + ip);
        sender.sendMessage("§7Accounts found: §e" + alts.size());
        sender.sendMessage("");

        if (alts.size() > 1) {
            sender.sendMessage("§c§lPotential Alt Accounts:");
            for (DatabaseManager.PlayerIPRecord record : alts) {
                boolean isBanned = databaseManager.isBanned(record.uuid);
                boolean isMuted = databaseManager.isMuted(record.uuid);

                String status = "";
                if (isBanned) status += " §c[BANNED]";
                if (isMuted) status += " §e[MUTED]";

                sender.sendMessage("§7  • §f" + record.playerName + status);
                sender.sendMessage("§7    Last seen: §f" + new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm").format(new java.util.Date(record.lastSeen)));
            }
        } else {
            sender.sendMessage("§aNo alt accounts detected.");
        }

        // Check if IP is banned/muted
        if (databaseManager.isIPBanned(ip)) {
            sender.sendMessage("");
            sender.sendMessage("§c§l⚠ This IP address is BANNED!");
            DatabaseManager.IPBanInfo ipBan = databaseManager.getIPBan(ip);
            if (ipBan != null) {
                sender.sendMessage("§7Reason: §f" + ipBan.reason);
                sender.sendMessage("§7Banned by: §f" + ipBan.issuerName);
            }
        }

        if (databaseManager.isIPMuted(ip)) {
            sender.sendMessage("");
            sender.sendMessage("§e§l⚠ This IP address is MUTED!");
            DatabaseManager.IPMuteInfo ipMute = databaseManager.getIPMute(ip);
            if (ipMute != null) {
                sender.sendMessage("§7Reason: §f" + ipMute.reason);
                sender.sendMessage("§7Muted by: §f" + ipMute.issuerName);
            }
        }

        sender.sendMessage("§6§l" + "=".repeat(50));

        return true;
    }
}