package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class CheckStaff implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    public CheckStaff(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.checkstaff")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /checkstaff <staff_name>");
            sender.sendMessage("§7View all punishments issued by a staff member");
            return true;
        }

        String staffName = args[0];
        UUID staffUUID = Bukkit.getOfflinePlayer(staffName).getUniqueId();

        DatabaseManager.StaffActivity activity = databaseManager.getStaffActivity(staffUUID, staffName);

        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("§6§lStaff Activity: §e" + staffName);
        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("");

        sender.sendMessage("§7Total Punishments Issued: §e" + activity.getTotalPunishments());
        sender.sendMessage("§c  Bans: " + activity.getBansIssued().size());
        sender.sendMessage("§e  Mutes: " + activity.getMutesIssued().size());
        sender.sendMessage("§6  Kicks: " + activity.getKicksIssued().size());
        sender.sendMessage("§d  Warnings: " + activity.getWarningsIssued().size());
        sender.sendMessage("");

        // Recent bans
        if (!activity.getBansIssued().isEmpty() && activity.getBansIssued().size() <= 5) {
            sender.sendMessage("§c§lRecent Bans:");
            for (DatabaseManager.BanInfo ban : activity.getBansIssued()) {
                sender.sendMessage("§7  • §f" + ban.playerName + " §7- §f" + ban.reason);
                sender.sendMessage("§7    " + dateFormat.format(new Date(ban.issuedAt)) + " §7(ID: " + ban.id + ")");
            }
            sender.sendMessage("");
        } else if (activity.getBansIssued().size() > 5) {
            sender.sendMessage("§c§lRecent Bans (Last 5):");
            for (int i = 0; i < 5 && i < activity.getBansIssued().size(); i++) {
                DatabaseManager.BanInfo ban = activity.getBansIssued().get(i);
                sender.sendMessage("§7  • §f" + ban.playerName + " §7- §f" + ban.reason);
            }
            sender.sendMessage("");
        }

        // Recent mutes
        if (!activity.getMutesIssued().isEmpty() && activity.getMutesIssued().size() <= 5) {
            sender.sendMessage("§e§lRecent Mutes:");
            for (DatabaseManager.MuteInfo mute : activity.getMutesIssued()) {
                sender.sendMessage("§7  • §f" + mute.playerName + " §7- §f" + mute.reason);
                sender.sendMessage("§7    " + dateFormat.format(new Date(mute.issuedAt)) + " §7(ID: " + mute.id + ")");
            }
            sender.sendMessage("");
        }

        // Recent kicks
        if (!activity.getKicksIssued().isEmpty() && activity.getKicksIssued().size() <= 5) {
            sender.sendMessage("§6§lRecent Kicks:");
            for (DatabaseManager.KickInfo kick : activity.getKicksIssued()) {
                sender.sendMessage("§7  • §f" + kick.playerName + " §7- §f" + kick.reason);
                sender.sendMessage("§7    " + dateFormat.format(new Date(kick.issuedAt)) + " §7(ID: " + kick.id + ")");
            }
            sender.sendMessage("");
        }

        // Recent warnings
        if (!activity.getWarningsIssued().isEmpty() && activity.getWarningsIssued().size() <= 5) {
            sender.sendMessage("§d§lRecent Warnings:");
            for (DatabaseManager.WarningInfo warn : activity.getWarningsIssued()) {
                sender.sendMessage("§7  • §f" + warn.playerName + " §7- §f" + warn.reason);
                sender.sendMessage("§7    " + dateFormat.format(new Date(warn.issuedAt)) + " §7(ID: " + warn.id + ")");
            }
        }

        sender.sendMessage("§6§l" + "=".repeat(50));

        return true;
    }
}