package org.auth.duckyPunishment;

import org.auth.duckyPunishment.commands.*;
import org.auth.duckyPunishment.guis.Main;
import org.auth.duckyPunishment.listeners.GUIListener;
import org.auth.duckyPunishment.listeners.PlayerListener;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.webserver.WebServerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class DuckyPunishment extends JavaPlugin {

    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private WebServerManager webServerManager;
    private Main mainGUI;

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);

        // Connect to database
        databaseManager = new DatabaseManager(
                configManager.getDatabaseHost(),
                configManager.getDatabasePort(),
                configManager.getDatabaseName(),
                configManager.getDatabaseUsername(),
                configManager.getDatabasePassword()
        );
        databaseManager.connect();

        // Initialize GUI
        mainGUI = new Main(databaseManager, configManager);

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        // Register PlaceholderAPI if available
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new org.auth.duckyPunishment.placeholders.DuckyPlaceholders(this).register();
            getLogger().info("PlaceholderAPI hooked!");
        }

        // Start web server
        if (configManager.isWebServerEnabled()) {
            webServerManager = new WebServerManager(databaseManager, configManager.getWebServerPort());
            webServerManager.start();
        }

        // Log startup
        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("║   DuckyPunishment has been enabled!  ║");
        getLogger().info("║   Version: " + getDescription().getVersion() + "                       ║");
        getLogger().info("║   Multi-server punishment system     ║");
        getLogger().info("╚══════════════════════════════════════╝");

        if (configManager.isWebServerEnabled()) {
            getLogger().info("Web Interface: http://localhost:" + configManager.getWebServerPort());
        }
    }

    @Override
    public void onDisable() {
        // Stop web server
        if (webServerManager != null) {
            webServerManager.stop();
        }

        // Disconnect from database
        if (databaseManager != null) {
            databaseManager.disconnect();
        }

        getLogger().info("DuckyPunishment has been disabled!");
    }

    private void registerCommands() {
        // Ban commands
        registerCommand("ban", new Ban(databaseManager, configManager));
        registerCommand("tempban", new TempBan(databaseManager, configManager));
        registerCommand("unban", new Unban(databaseManager, configManager));

        // Mute commands
        registerCommand("mute", new Mute(databaseManager, configManager));
        registerCommand("tempmute", new TempMute(databaseManager, configManager));
        registerCommand("unmute", new Unmute(databaseManager, configManager));

        // Kick command
        registerCommand("kick", new Kick(databaseManager, configManager));

        // Warn command
        registerCommand("warn", new Warn(databaseManager, configManager));

        // IP Ban commands
        registerCommand("ipban", new IPBan(databaseManager, configManager));
        registerCommand("ipunban", new IPUnban(databaseManager, configManager));

        // IP Mute commands
        registerCommand("ipmute", new IPMute(databaseManager, configManager));
        registerCommand("ipunmute", new IPUnmute(databaseManager, configManager));

        // GUI command
        registerCommand("punish", new Punish(mainGUI));

        // Utility commands
        registerCommand("history", new punishmentHistory(databaseManager, configManager));
        registerCommand("alts", new Alts(databaseManager, configManager));
        registerCommand("duckyreload", new Reload(this));
        registerCommand("punishmentclear", new PunishmentClear(databaseManager, configManager));
        registerCommand("clearpunishment", new ClearPunishment(databaseManager, configManager));
        registerCommand("checkstaff", new CheckStaff(databaseManager, configManager));
        registerCommand("duckyhelp", new DuckyHelp());
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        } else {
            getLogger().warning("Failed to register command: " + name + " (not defined in plugin.yml)");
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(databaseManager, configManager), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(databaseManager, configManager, mainGUI), this);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WebServerManager getWebServerManager() {
        return webServerManager;
    }
}

// Unban command
class Unban implements org.bukkit.command.CommandExecutor {
    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public Unban(DatabaseManager db, ConfigManager cm) {
        this.databaseManager = db;
        this.configManager = cm;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.unban")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /unban <player> [reason]");
            return true;
        }

        String targetName = args[0];
        java.util.UUID targetUUID = org.bukkit.Bukkit.getOfflinePlayer(targetName).getUniqueId();

        if (!databaseManager.isBanned(targetUUID)) {
            sender.sendMessage("§c" + targetName + " is not banned!");
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();
        if (reason.isEmpty()) reason = "No reason provided";

        String issuer = sender instanceof org.bukkit.entity.Player ? sender.getName() : "Console";
        databaseManager.removeBan(targetUUID, issuer, reason);

        String notification = configManager.getMessage("unban-notification")
                .replace("%player%", targetName)
                .replace("%issuer%", issuer)
                .replace("%reason%", reason);

        org.bukkit.Bukkit.broadcast(notification, "duckypunishment.notify");
        sender.sendMessage(notification);

        return true;
    }
}

// Unmute command
class Unmute implements org.bukkit.command.CommandExecutor {
    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public Unmute(DatabaseManager db, ConfigManager cm) {
        this.databaseManager = db;
        this.configManager = cm;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duckypunishment.unmute")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /unmute <player> [reason]");
            return true;
        }

        String targetName = args[0];
        java.util.UUID targetUUID = org.bukkit.Bukkit.getOfflinePlayer(targetName).getUniqueId();

        if (!databaseManager.isMuted(targetUUID)) {
            sender.sendMessage("§c" + targetName + " is not muted!");
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();
        if (reason.isEmpty()) reason = "No reason provided";

        String issuer = sender instanceof org.bukkit.entity.Player ? sender.getName() : "Console";
        databaseManager.removeMute(targetUUID, issuer, reason);

        org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(targetName);
        if (target != null && target.isOnline()) {
            target.sendMessage("§aYou have been unmuted by " + issuer);
        }

        String notification = configManager.getMessage("unmute-notification")
                .replace("%player%", targetName)
                .replace("%issuer%", issuer)
                .replace("%reason%", reason);

        org.bukkit.Bukkit.broadcast(notification, "duckypunishment.notify");
        sender.sendMessage(notification);

        return true;
    }
}