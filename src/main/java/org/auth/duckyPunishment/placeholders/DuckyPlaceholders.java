package org.auth.duckyPunishment.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.auth.duckyPunishment.DuckyPunishment;
import org.auth.duckyPunishment.managers.DatabaseManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DuckyPlaceholders extends PlaceholderExpansion {

    private final DuckyPunishment plugin;
    private final DatabaseManager databaseManager;

    public DuckyPlaceholders(DuckyPunishment plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "duckypunishment";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // %duckypunishment_isbanned%
        if (params.equalsIgnoreCase("isbanned")) {
            return databaseManager.isBanned(player.getUniqueId()) ? "Yes" : "No";
        }

        // %duckypunishment_ismuted%
        if (params.equalsIgnoreCase("ismuted")) {
            return databaseManager.isMuted(player.getUniqueId()) ? "Yes" : "No";
        }

        // %duckypunishment_ban_reason%
        if (params.equalsIgnoreCase("ban_reason")) {
            DatabaseManager.BanInfo ban = databaseManager.getActiveBan(player.getUniqueId());
            return ban != null ? ban.reason : "Not banned";
        }

        // %duckypunishment_mute_reason%
        if (params.equalsIgnoreCase("mute_reason")) {
            DatabaseManager.MuteInfo mute = databaseManager.getActiveMute(player.getUniqueId());
            return mute != null ? mute.reason : "Not muted";
        }

        // %duckypunishment_ban_time_left%
        if (params.equalsIgnoreCase("ban_time_left")) {
            DatabaseManager.BanInfo ban = databaseManager.getActiveBan(player.getUniqueId());
            if (ban != null) {
                if (ban.expiresAt == -1) {
                    return "Permanent";
                }
                long remaining = ban.expiresAt - System.currentTimeMillis();
                if (remaining <= 0) {
                    return "Expired";
                }
                return formatDuration(remaining);
            }
            return "Not banned";
        }

        // %duckypunishment_mute_time_left%
        if (params.equalsIgnoreCase("mute_time_left")) {
            DatabaseManager.MuteInfo mute = databaseManager.getActiveMute(player.getUniqueId());
            if (mute != null) {
                if (mute.expiresAt == -1) {
                    return "Permanent";
                }
                long remaining = mute.expiresAt - System.currentTimeMillis();
                if (remaining <= 0) {
                    return "Expired";
                }
                return formatDuration(remaining);
            }
            return "Not muted";
        }

        // %duckypunishment_ban_duration%
        if (params.equalsIgnoreCase("ban_duration")) {
            DatabaseManager.BanInfo ban = databaseManager.getActiveBan(player.getUniqueId());
            if (ban != null) {
                if (ban.expiresAt == -1) {
                    return "Permanent";
                }
                long duration = ban.expiresAt - ban.issuedAt;
                return formatDuration(duration);
            }
            return "Not banned";
        }

        // %duckypunishment_mute_duration%
        if (params.equalsIgnoreCase("mute_duration")) {
            DatabaseManager.MuteInfo mute = databaseManager.getActiveMute(player.getUniqueId());
            if (mute != null) {
                if (mute.expiresAt == -1) {
                    return "Permanent";
                }
                long duration = mute.expiresAt - mute.issuedAt;
                return formatDuration(duration);
            }
            return "Not muted";
        }

        // %duckypunishment_total_bans%
        if (params.equalsIgnoreCase("total_bans")) {
            DatabaseManager.PunishmentHistory history = databaseManager.getHistory(player.getUniqueId());
            return String.valueOf(history.getBans().size());
        }

        // %duckypunishment_total_mutes%
        if (params.equalsIgnoreCase("total_mutes")) {
            DatabaseManager.PunishmentHistory history = databaseManager.getHistory(player.getUniqueId());
            return String.valueOf(history.getMutes().size());
        }

        // %duckypunishment_total_kicks%
        if (params.equalsIgnoreCase("total_kicks")) {
            DatabaseManager.PunishmentHistory history = databaseManager.getHistory(player.getUniqueId());
            return String.valueOf(history.getKicks().size());
        }

        // %duckypunishment_total_warnings%
        if (params.equalsIgnoreCase("total_warnings")) {
            DatabaseManager.PunishmentHistory history = databaseManager.getHistory(player.getUniqueId());
            return String.valueOf(history.getWarnings().size());
        }

        // %duckypunishment_total_punishments%
        if (params.equalsIgnoreCase("total_punishments")) {
            DatabaseManager.PunishmentHistory history = databaseManager.getHistory(player.getUniqueId());
            return String.valueOf(history.getTotalPunishments());
        }

        // %duckypunishment_warning_count%
        if (params.equalsIgnoreCase("warning_count")) {
            return String.valueOf(databaseManager.getWarningCount(player.getUniqueId()));
        }

        return null;
    }

    private String formatDuration(long duration) {
        if (duration <= 0) return "Expired";

        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        if (years > 0) return years + "y";
        if (months > 0) return months + "mo";
        if (weeks > 0) return weeks + "w";
        if (days > 0) return days + "d";
        if (hours > 0) return hours + "h";
        if (minutes > 0) return minutes + "m";
        return seconds + "s";
    }
}