package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.ConfigManager;
import org.auth.duckyPunishment.managers.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

// Separate command for clearing by ID
public class ClearPunishment implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public ClearPunishment(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.clear")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /clearpunishment <type> <id>");
            sender.sendMessage("§7Types: ban, mute, kick, warn");
            sender.sendMessage("§7Example: /clearpunishment ban 42");
            return true;
        }

        String type = args[0].toLowerCase();
        int id;

        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid ID! Must be a number.");
            return true;
        }

        boolean success = false;

        switch (type) {
            case "ban":
                success = databaseManager.clearBanById(id);
                break;
            case "mute":
                success = databaseManager.clearMuteById(id);
                break;
            case "kick":
                success = databaseManager.clearKickById(id);
                break;
            case "warn":
            case "warning":
                success = databaseManager.clearWarningById(id);
                break;
            default:
                sender.sendMessage("§cInvalid type! Use: ban, mute, kick, or warn");
                return true;
        }

        if (success) {
            sender.sendMessage("§aSuccessfully cleared " + type + " with ID: " + id);
            Bukkit.broadcast("§c" + sender.getName() + " §7cleared " + type + " §e#" + id, "duckypunishment.notify");
        } else {
            sender.sendMessage("§cFailed to clear punishment! ID not found or already cleared.");
        }

        return true;
    }
}
