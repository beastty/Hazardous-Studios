package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.ConfigManager;
import org.auth.duckyPunishment.managers.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// IP Unban Command
public class IPUnban implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public IPUnban(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.ipban")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /ipunban <ip>");
            return true;
        }

        String ip = args[0];

        if (!databaseManager.isIPBanned(ip)) {
            sender.sendMessage("§cThis IP address is not banned!");
            return true;
        }

        String issuer = sender instanceof Player ? sender.getName() : "Console";
        databaseManager.removeIPBan(ip, issuer);

        String notification = "§a" + ip + " §7has been IP-unbanned by §a" + issuer;
        Bukkit.broadcast(notification, "duckypunishment.notify");
        sender.sendMessage(notification);

        return true;
    }
}
