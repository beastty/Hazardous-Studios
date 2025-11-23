package org.auth.duckyPunishment.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigManager {

    private final Plugin plugin;
    private FileConfiguration config;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public String getMessage(String path) {
        String message = config.getString("messages." + path);
        if (message == null) {
            return getDefaultMessage(path);
        }
        return message.replace("&", "§");
    }

    private String getDefaultMessage(String path) {
        switch (path) {
            case "no-permission":
                return "§cYou don't have permission to execute this command!";
            case "default-ban-reason":
                return "Banned by an operator";
            case "default-mute-reason":
                return "Muted by an operator";
            case "default-kick-reason":
                return "Kicked by an operator";
            case "ban-notification":
                return "§c%player% §7has been banned by §c%issuer% §7for §e%duration% §7- §f%reason%";
            case "mute-notification":
                return "§e%player% §7has been muted by §e%issuer% §7for §e%duration% §7- §f%reason%";
            case "kick-notification":
                return "§6%player% §7has been kicked by §6%issuer% §7- §f%reason%";
            case "unban-notification":
                return "§a%player% §7has been unbanned by §a%issuer% §7- §f%reason%";
            case "unmute-notification":
                return "§a%player% §7has been unmuted by §a%issuer% §7- §f%reason%";
            case "appeal-website":
                return "https://yourserver.com/appeal";
            case "player-muted":
                return "§cYou are muted! Remaining time: %time%";
            default:
                return "§cMessage not found: " + path;
        }
    }

    // Database Configuration
    public String getDatabaseHost() {
        return config.getString("database.host", "localhost");
    }

    public String getDatabasePort() {
        return config.getString("database.port", "3306");
    }

    public String getDatabaseName() {
        return config.getString("database.database", "duckypunishment");
    }

    public String getDatabaseUsername() {
        return config.getString("database.username", "root");
    }

    public String getDatabasePassword() {
        return config.getString("database.password", "password");
    }

    // Feature Settings
    public boolean isBroadcastPunishments() {
        return config.getBoolean("settings.broadcast-punishments", true);
    }

    public boolean isSyncAcrossServers() {
        return config.getBoolean("settings.sync-across-servers", false);
    }

    public boolean isPreventMutedCommands() {
        return config.getBoolean("settings.prevent-muted-commands", true);
    }

    // Web Server Settings
    public boolean isWebServerEnabled() {
        return config.getBoolean("webserver.enabled", true);
    }

    public int getWebServerPort() {
        return config.getInt("webserver.port", 8080);
    }

    // Hierarchy System
    public boolean canPunish(org.bukkit.entity.Player punisher, org.bukkit.entity.Player target) {
        if (!config.getBoolean("settings.hierarchy-enabled", true)) {
            return true; // Hierarchy disabled
        }

        // Get hierarchy levels from config
        int punisherLevel = getHierarchyLevel(punisher);
        int targetLevel = getHierarchyLevel(target);

        // Can punish if punisher has higher or equal level
        return punisherLevel >= targetLevel;
    }

    private int getHierarchyLevel(org.bukkit.entity.Player player) {
        // Check permissions in order of hierarchy
        if (player.hasPermission("duckypunishment.hierarchy.owner")) return 100;
        if (player.hasPermission("duckypunishment.hierarchy.admin")) return 80;
        if (player.hasPermission("duckypunishment.hierarchy.moderator")) return 60;
        if (player.hasPermission("duckypunishment.hierarchy.helper")) return 40;
        if (player.hasPermission("duckypunishment.hierarchy.staff")) return 20;
        return 0; // Regular player
    }
}