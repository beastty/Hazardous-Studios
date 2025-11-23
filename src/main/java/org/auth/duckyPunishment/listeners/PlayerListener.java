package org.auth.duckyPunishment.listeners;

import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    public PlayerListener(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        String ip = e.getAddress().getHostAddress();

        // Track IP
        databaseManager.trackIP(uuid, e.getPlayer().getName(), ip);

        boolean notificationSent = false;

        // Check if IP is banned FIRST (takes priority)
        if (databaseManager.isIPBanned(ip)) {
            DatabaseManager.IPBanInfo ipBan = databaseManager.getIPBan(ip);
            if (ipBan != null) {
                String kickMessage = formatIPBanMessage(ipBan);
                e.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMessage);

                String staffNotification = "§c§l[!] §c" + e.getPlayer().getName() + " §7tried to join but IP §c" + ip + " §7is banned";
                Bukkit.broadcast(staffNotification, "duckypunishment.notify");
                notificationSent = true;
            }
        }
        // Only check player ban if IP is not banned
        else if (databaseManager.isBanned(uuid)) {
            DatabaseManager.BanInfo ban = databaseManager.getActiveBan(uuid);
            if (ban != null) {
                String kickMessage = formatBanMessage(ban);
                e.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMessage);

                // Check for ban evasion (other accounts on same IP are banned)
                List<DatabaseManager.PlayerIPRecord> alts = databaseManager.getAccountsByIP(ip);
                boolean hasEvasion = false;
                String evadingAccount = "";

                for (DatabaseManager.PlayerIPRecord alt : alts) {
                    if (!alt.uuid.equals(uuid) && databaseManager.isBanned(alt.uuid)) {
                        hasEvasion = true;
                        evadingAccount = alt.playerName;
                        break;
                    }
                }

                String staffNotification;
                if (hasEvasion) {
                    staffNotification = "§c§l[!] §c" + e.getPlayer().getName() + " §7tried to join but is §cbanned §7(§ePossible evasion of §c" + evadingAccount + "§7's ban - IP: §c" + ip + "§7)";
                } else {
                    staffNotification = "§c§l[!] §c" + e.getPlayer().getName() + " §7tried to join but is §cbanned §7(Reason: " + ban.reason + ")";
                }
                Bukkit.broadcast(staffNotification, "duckypunishment.notify");
                notificationSent = true;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        String ip = e.getPlayer().getAddress().getAddress().getHostAddress();

        // Check if IP is muted FIRST (takes priority)
        if (databaseManager.isIPMuted(ip)) {
            DatabaseManager.IPMuteInfo ipMute = databaseManager.getIPMute(ip);
            if (ipMute != null) {
                e.setCancelled(true);
                String muteMessage = formatIPMuteMessage(ipMute);
                e.getPlayer().sendMessage(muteMessage);

                String staffNotification = "§e§l[!] §e" + e.getPlayer().getName() + " §7tried to chat but IP §e" + ip + " §7is muted";
                Bukkit.broadcast(staffNotification, "duckypunishment.notify");
                return;
            }
        }

        // Only check player mute if IP is not muted
        if (databaseManager.isMuted(uuid)) {
            DatabaseManager.MuteInfo mute = databaseManager.getActiveMute(uuid);
            if (mute != null) {
                e.setCancelled(true);
                String muteMessage = formatMuteMessage(mute);
                e.getPlayer().sendMessage(muteMessage);

                // Check for mute evasion (other accounts on same IP are muted)
                List<DatabaseManager.PlayerIPRecord> alts = databaseManager.getAccountsByIP(ip);
                boolean hasEvasion = false;
                String evadingAccount = "";

                for (DatabaseManager.PlayerIPRecord alt : alts) {
                    if (!alt.uuid.equals(uuid) && databaseManager.isMuted(alt.uuid)) {
                        hasEvasion = true;
                        evadingAccount = alt.playerName;
                        break;
                    }
                }

                String staffNotification;
                if (hasEvasion) {
                    staffNotification = "§e§l[!] §e" + e.getPlayer().getName() + " §7tried to chat but is §emuted §7(§6Possible evasion of §e" + evadingAccount + "§7's mute - IP: §e" + ip + "§7)";
                } else {
                    staffNotification = "§e§l[!] §e" + e.getPlayer().getName() + " §7tried to chat but is §emuted §7(Reason: " + mute.reason + ")";
                }
                Bukkit.broadcast(staffNotification, "duckypunishment.notify");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();

        // Check if player is muted and trying to use commands
        if (configManager.isPreventMutedCommands() && databaseManager.isMuted(uuid)) {
            String command = e.getMessage().toLowerCase();

            // Block certain commands while muted (msg, tell, r, etc.)
            if (command.startsWith("/msg") || command.startsWith("/tell") ||
                    command.startsWith("/w") || command.startsWith("/whisper") ||
                    command.startsWith("/r") || command.startsWith("/reply") ||
                    command.startsWith("/me")) {

                e.setCancelled(true);
                DatabaseManager.MuteInfo mute = databaseManager.getActiveMute(uuid);
                if (mute != null) {
                    String muteMessage = formatMuteMessage(mute);
                    e.getPlayer().sendMessage(muteMessage);
                }
            }
        }
    }

    private String formatBanMessage(DatabaseManager.BanInfo ban) {
        StringBuilder msg = new StringBuilder();
        msg.append("§c§lYou are banned from this server!\n\n");
        msg.append("§7Player: §f").append(ban.playerName).append("\n");
        msg.append("§7Banned by: §f").append(ban.issuerName).append("\n");
        msg.append("§7Reason: §f").append(ban.reason).append("\n");
        msg.append("§7Date: §f").append(dateFormat.format(new Date(ban.issuedAt))).append("\n");

        if (ban.expiresAt == -1) {
            msg.append("§7Duration: §cPermanent\n");
        } else {
            long remaining = ban.expiresAt - System.currentTimeMillis();
            msg.append("§7Expires in: §f").append(formatDuration(remaining)).append("\n");
            msg.append("§7Expires on: §f").append(dateFormat.format(new Date(ban.expiresAt))).append("\n");
        }

        msg.append("\n§7Appeal at: §f").append(configManager.getMessage("appeal-website"));

        return msg.toString();
    }

    private String formatMuteMessage(DatabaseManager.MuteInfo mute) {
        StringBuilder msg = new StringBuilder();
        msg.append("§c§lYou are muted!\n");
        msg.append("§7Reason: §f").append(mute.reason).append("\n");

        if (mute.expiresAt == -1) {
            msg.append("§7Duration: §cPermanent");
        } else {
            long remaining = mute.expiresAt - System.currentTimeMillis();
            msg.append("§7Time remaining: §f").append(formatDuration(remaining));
        }

        return msg.toString();
    }

    private String formatIPBanMessage(DatabaseManager.IPBanInfo ipBan) {
        StringBuilder msg = new StringBuilder();
        msg.append("§c§lYour IP address is banned from this server!\n\n");
        msg.append("§7IP Address: §f").append(ipBan.ipAddress).append("\n");
        msg.append("§7Banned by: §f").append(ipBan.issuerName).append("\n");
        msg.append("§7Reason: §f").append(ipBan.reason).append("\n");
        msg.append("§7Date: §f").append(dateFormat.format(new Date(ipBan.issuedAt))).append("\n");

        if (ipBan.expiresAt == -1) {
            msg.append("§7Duration: §cPermanent\n");
        } else {
            long remaining = ipBan.expiresAt - System.currentTimeMillis();
            msg.append("§7Expires in: §f").append(formatDuration(remaining)).append("\n");
            msg.append("§7Expires on: §f").append(dateFormat.format(new Date(ipBan.expiresAt))).append("\n");
        }

        msg.append("\n§7Appeal at: §f").append(configManager.getMessage("appeal-website"));

        return msg.toString();
    }

    private String formatIPMuteMessage(DatabaseManager.IPMuteInfo ipMute) {
        StringBuilder msg = new StringBuilder();
        msg.append("§c§lYour IP address is muted!\n");
        msg.append("§7Reason: §f").append(ipMute.reason).append("\n");

        if (ipMute.expiresAt == -1) {
            msg.append("§7Duration: §cPermanent");
        } else {
            long remaining = ipMute.expiresAt - System.currentTimeMillis();
            msg.append("§7Time remaining: §f").append(formatDuration(remaining));
        }

        return msg.toString();
    }

    private String formatDuration(long duration) {
        if (duration <= 0) return "Expired";

        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + " day" + (days > 1 ? "s" : "");
        if (hours > 0) return hours + " hour" + (hours > 1 ? "s" : "");
        if (minutes > 0) return minutes + " minute" + (minutes > 1 ? "s" : "");
        return seconds + " second" + (seconds > 1 ? "s" : "");
    }
}