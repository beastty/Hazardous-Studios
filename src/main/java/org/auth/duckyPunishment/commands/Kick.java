package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Kick implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public Kick(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.kick")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /kick <player> [reason]");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cPlayer " + targetName + " is not online!");
            return true;
        }

        UUID targetUUID = target.getUniqueId();

        // Parse reason
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();
        if (reason.isEmpty()) {
            reason = configManager.getMessage("default-kick-reason");
        }

        String issuer = sender instanceof Player ? sender.getName() : "Console";
        UUID issuerUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        // Add kick to database
        databaseManager.addKick(targetUUID, targetName, issuerUUID, issuer, reason);

        // Kick player
        String kickMessage = formatKickMessage(targetName, reason, issuer);
        target.kickPlayer(kickMessage);

        // Notify staff
        String notification = configManager.getMessage("kick-notification")
                .replace("%player%", targetName)
                .replace("%issuer%", issuer)
                .replace("%reason%", reason);

        Bukkit.broadcast(notification, "duckypunishment.notify");
        sender.sendMessage(notification);

        return true;
    }

    private String formatKickMessage(String player, String reason, String issuer) {
        StringBuilder msg = new StringBuilder();
        msg.append("§c§lYou have been kicked!\n\n");
        msg.append("§7Player: §f").append(player).append("\n");
        msg.append("§7Kicked by: §f").append(issuer).append("\n");
        msg.append("§7Reason: §f").append(reason).append("\n");
        return msg.toString();
    }
}