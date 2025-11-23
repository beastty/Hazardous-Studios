package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class PunishmentClear implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public PunishmentClear(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.clear")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /punishmentclear <player> [type|all]");
            sender.sendMessage("§7Types: ban, mute, kick, warn");
            sender.sendMessage("§7Example: /punishmentclear Steve ban");
            sender.sendMessage("§7Example: /punishmentclear Steve all");
            return true;
        }

        String targetName = args[0];
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        if (args.length == 1) {
            // Show punishment summary
            showPunishmentSummary(sender, targetName, targetUUID);
            return true;
        }

        String type = args[1].toLowerCase();

        switch (type) {
            case "all":
                clearAllPunishments(sender, targetName, targetUUID);
                break;
            case "ban":
            case "bans":
                databaseManager.clearBans(targetUUID);
                sender.sendMessage("§aCleared all bans for " + targetName);
                break;
            case "mute":
            case "mutes":
                databaseManager.clearMutes(targetUUID);
                sender.sendMessage("§aCleared all mutes for " + targetName);
                break;
            case "kick":
            case "kicks":
                databaseManager.clearKicks(targetUUID);
                sender.sendMessage("§aCleared all kicks for " + targetName);
                break;
            case "warn":
            case "warning":
            case "warnings":
                databaseManager.clearWarnings(targetUUID);
                sender.sendMessage("§aCleared all warnings for " + targetName);
                break;
            default:
                sender.sendMessage("§cInvalid type! Use: ban, mute, kick, warn, or all");
                break;
        }

        return true;
    }

    private void showPunishmentSummary(CommandSender sender, String targetName, UUID targetUUID) {
        DatabaseManager.PunishmentHistory history = databaseManager.getHistory(targetUUID);

        sender.sendMessage("§6§l" + "=".repeat(40));
        sender.sendMessage("§6§lPunishment Summary: §e" + targetName);
        sender.sendMessage("§6§l" + "=".repeat(40));
        sender.sendMessage("§7Bans: §c" + history.getBans().size());
        sender.sendMessage("§7Mutes: §e" + history.getMutes().size());
        sender.sendMessage("§7Kicks: §6" + history.getKicks().size());
        sender.sendMessage("§7Warnings: §d" + history.getWarnings().size());
        sender.sendMessage("§7Total: §f" + history.getTotalPunishments());
        sender.sendMessage("§6§l" + "=".repeat(40));
        sender.sendMessage("§eUse: /punishmentclear " + targetName + " <type|all>");
    }

    private void clearAllPunishments(CommandSender sender, String targetName, UUID targetUUID) {
        databaseManager.clearBans(targetUUID);
        databaseManager.clearMutes(targetUUID);
        databaseManager.clearKicks(targetUUID);
        databaseManager.clearWarnings(targetUUID);

        sender.sendMessage("§aCleared ALL punishments for " + targetName);
        sender.sendMessage("§7All bans, mutes, kicks, and warnings have been removed.");

        // Log to staff
        String message = "§c" + sender.getName() + " §7cleared all punishments for §e" + targetName;
        Bukkit.broadcast(message, "duckypunishment.notify");
    }
}

