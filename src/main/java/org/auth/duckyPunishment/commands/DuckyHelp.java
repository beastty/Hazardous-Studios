package org.auth.duckyPunishment.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DuckyHelp implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        int page = 1;

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        if (page == 1) {
            showPage1(sender);
        } else if (page == 2) {
            showPage2(sender);
        } else if (page == 3) {
            showPage3(sender);
        } else {
            sender.sendMessage("§cInvalid page! Use /duckyhelp 1, 2, or 3");
        }

        return true;
    }

    private void showPage1(CommandSender sender) {
        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("§6§l         DuckyPunishment Help - Page 1/3");
        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("");
        sender.sendMessage("§e§lBan Commands:");
        sender.sendMessage("§7/ban <player> [duration] [reason] [-s]");
        sender.sendMessage("§f  Ban a player permanently or temporarily");
        sender.sendMessage("§7/tempban <player> <duration> [reason] [-s]");
        sender.sendMessage("§f  Temporarily ban a player");
        sender.sendMessage("§7/unban <player> [reason]");
        sender.sendMessage("§f  Unban a player");
        sender.sendMessage("");
        sender.sendMessage("§e§lMute Commands:");
        sender.sendMessage("§7/mute <player> [duration] [reason] [-s]");
        sender.sendMessage("§f  Mute a player permanently or temporarily");
        sender.sendMessage("§7/tempmute <player> <duration> [reason] [-s]");
        sender.sendMessage("§f  Temporarily mute a player");
        sender.sendMessage("§7/unmute <player> [reason]");
        sender.sendMessage("§f  Unmute a player");
        sender.sendMessage("");
        sender.sendMessage("§7Use §e/duckyhelp 2 §7for more commands");
        sender.sendMessage("§6§l" + "=".repeat(50));
    }

    private void showPage2(CommandSender sender) {
        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("§6§l         DuckyPunishment Help - Page 2/3");
        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("");
        sender.sendMessage("§e§lOther Punishment Commands:");
        sender.sendMessage("§7/kick <player> [reason] [-s]");
        sender.sendMessage("§f  Kick a player from the server");
        sender.sendMessage("§7/warn <player> [reason] [-s]");
        sender.sendMessage("§f  Warn a player");
        sender.sendMessage("");
        sender.sendMessage("§e§lIP Punishments:");
        sender.sendMessage("§7/ipban <player|ip> [duration] [reason] [-s]");
        sender.sendMessage("§f  Ban an IP address");
        sender.sendMessage("§7/ipunban <ip>");
        sender.sendMessage("§f  Unban an IP address");
        sender.sendMessage("§7/ipmute <player|ip> [duration] [reason] [-s]");
        sender.sendMessage("§f  Mute an IP address");
        sender.sendMessage("§7/ipunmute <ip>");
        sender.sendMessage("§f  Unmute an IP address");
        sender.sendMessage("");
        sender.sendMessage("§7Use §e/duckyhelp 3 §7for more commands");
        sender.sendMessage("§6§l" + "=".repeat(50));
    }

    private void showPage3(CommandSender sender) {
        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("§6§l         DuckyPunishment Help - Page 3/3");
        sender.sendMessage("§6§l" + "=".repeat(50));
        sender.sendMessage("");
        sender.sendMessage("§e§lGUI & Checking:");
        sender.sendMessage("§7/punish <player>");
        sender.sendMessage("§f  Open punishment GUI for a player");
        sender.sendMessage("§7/cpunishment <player>");
        sender.sendMessage("§f  Check current punishment status (with evasion detection)");
        sender.sendMessage("§7/history <player>");
        sender.sendMessage("§f  View a player's punishment history");
        sender.sendMessage("");
        sender.sendMessage("§e§lManagement Commands:");
        sender.sendMessage("§7/punishmentclear <player> [type|all]");
        sender.sendMessage("§f  Clear punishments (ban/mute/kick/warn/all)");
        sender.sendMessage("§7/clearpunishment <type> <id>");
        sender.sendMessage("§f  Clear a specific punishment by ID");
        sender.sendMessage("");
        sender.sendMessage("§e§lUtility Commands:");
        sender.sendMessage("§7/checkstaff <staff_name>");
        sender.sendMessage("§f  View all punishments issued by a staff member");
        sender.sendMessage("§7/alts <player>");
        sender.sendMessage("§f  Check for alternate accounts (IP-based)");
        sender.sendMessage("§7/duckyreload");
        sender.sendMessage("§f  Reload the plugin configuration");
        sender.sendMessage("");
        sender.sendMessage("§e§lWeb Dashboard:");
        sender.sendMessage("§7Access at: §fhttp://localhost:8080");
        sender.sendMessage("§7View bans, mutes, and player history online");
        sender.sendMessage("");
        sender.sendMessage("§7Use §e/duckyhelp 1 §7to go back");
        sender.sendMessage("§6§l" + "=".repeat(50));
    }
}