package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.managers.ConfigManager;
import org.auth.duckyPunishment.managers.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// IP Unmute Command
public class IPUnmute implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public IPUnmute(DatabaseManager databaseManager, ConfigManager configManager) {
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
            sender.sendMessage("§cUsage: /ipunmute <ip>");
            return true;
        }

        String ip = args[0];

        if (!databaseManager.isIPMuted(ip)) {
            sender.sendMessage("§cThis IP address is not muted!");
            return true;
        }

        String issuer = sender instanceof Player ? sender.getName() : "Console";
        databaseManager.removeIPMute(ip, issuer);

        // Notify affected players
        for (Player online : Bukkit.getOnlinePlayers()) {
            String playerIP = online.getAddress().getAddress().getHostAddress();
            if (playerIP.equals(ip)) {
                online.sendMessage("§aYour IP has been unmuted by " + issuer);
            }
        }

        String notification = "§a" + ip + " §7has been IP-unmuted by §a" + issuer;
        Bukkit.broadcast(notification, "duckypunishment.notify");
        sender.sendMessage(notification);

        return true;
    }
}
