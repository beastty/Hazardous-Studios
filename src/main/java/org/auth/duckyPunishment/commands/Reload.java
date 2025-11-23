package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.DuckyPunishment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Reload implements CommandExecutor {

    private final DuckyPunishment plugin;

    public Reload(DuckyPunishment plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.reload")) {
            sender.sendMessage("§cYou don't have permission to reload the plugin!");
            return true;
        }

        sender.sendMessage("§eReloading DuckyPunishment...");

        try {
            // Reload configuration
            plugin.getConfigManager().reloadConfig();

            sender.sendMessage("§aSuccessfully reloaded DuckyPunishment!");
            sender.sendMessage("§7- Configuration reloaded");
            sender.sendMessage("§7- Messages reloaded");
            sender.sendMessage("§7- Database connection maintained");
        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload plugin! Check console for errors.");
            e.printStackTrace();
        }

        return true;
    }
}