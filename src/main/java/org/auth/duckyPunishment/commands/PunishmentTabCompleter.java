package org.auth.duckyPunishment.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PunishmentTabCompleter implements TabCompleter {

    private static final List<String> DURATIONS = Arrays.asList(
            "30s", "1m", "5m", "10m", "15m", "30m",
            "1h", "3h", "6h", "12h",
            "1d", "3d", "7d", "14d", "30d",
            "1w", "2w", "1mo", "perm", "permanent"
    );

    private static final List<String> REASONS = Arrays.asList(
            "Hacking", "Cheating", "Spam", "Toxicity", "Language",
            "Advertising", "Griefing", "Harassment", "Exploiting",
            "Alt_Account", "Ban_Evasion", "Inappropriate_Name"
    );

    private static final List<String> PUNISHMENT_TYPES = Arrays.asList(
            "ban", "bans", "mute", "mutes", "kick", "kicks", "warn", "warning", "warnings", "all"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName().toLowerCase();

        // Player punishment commands
        if (cmdName.equals("ban") || cmdName.equals("tempban") ||
                cmdName.equals("mute") || cmdName.equals("tempmute") ||
                cmdName.equals("kick") || cmdName.equals("warn") ||
                cmdName.equals("ipban") || cmdName.equals("ipmute")) {

            if (args.length == 1) {
                // First arg: player name
                return getOnlinePlayerNames(args[0]);
            } else if (args.length == 2) {
                // Second arg: duration (for temp commands) or reason
                if (cmdName.contains("temp") || cmdName.equals("ipban") || cmdName.equals("ipmute")) {
                    return filterList(DURATIONS, args[1]);
                } else {
                    return filterList(REASONS, args[1]);
                }
            } else if (args.length == 3) {
                // Third arg: reason
                return filterList(REASONS, args[2]);
            } else if (args.length >= 4) {
                // Additional args: -s flag
                return Arrays.asList("-s");
            }
        }

        // Unban/unmute commands
        if (cmdName.equals("unban") || cmdName.equals("unmute") ||
                cmdName.equals("ipunban") || cmdName.equals("ipunmute")) {
            if (args.length == 1) {
                return getOnlinePlayerNames(args[0]);
            }
        }

        // History command
        if (cmdName.equals("history")) {
            if (args.length == 1) {
                return getOnlinePlayerNames(args[0]);
            }
        }

        // Punish command
        if (cmdName.equals("punish")) {
            if (args.length == 1) {
                return getOnlinePlayerNames(args[0]);
            }
        }

        // Alts command
        if (cmdName.equals("alts")) {
            if (args.length == 1) {
                return getOnlinePlayerNames(args[0]);
            }
        }

        // CheckStaff command
        if (cmdName.equals("checkstaff")) {
            if (args.length == 1) {
                return getOnlinePlayerNames(args[0]);
            }
        }

        // PunishmentClear command
        if (cmdName.equals("punishmentclear")) {
            if (args.length == 1) {
                return getOnlinePlayerNames(args[0]);
            } else if (args.length == 2) {
                return filterList(PUNISHMENT_TYPES, args[1]);
            }
        }

        // ClearPunishment command
        if (cmdName.equals("clearpunishment")) {
            if (args.length == 1) {
                return filterList(Arrays.asList("ban", "mute", "kick", "warn"), args[0]);
            }
            // args[1] would be ID number, no completion needed
        }

        // DuckyHelp command
        if (cmdName.equals("duckyhelp")) {
            if (args.length == 1) {
                return filterList(Arrays.asList("1", "2", "3"), args[0]);
            }
        }

        return new ArrayList<>();
    }

    private List<String> getOnlinePlayerNames(String prefix) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> filterList(List<String> list, String prefix) {
        return list.stream()
                .filter(item -> item.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}