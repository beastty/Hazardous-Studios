package org.auth.duckyPunishment.commands;

import org.auth.duckyPunishment.guis.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Punish implements CommandExecutor {

    private final Main mainGUI;

    public Punish(Main mainGUI) {
        this.mainGUI = mainGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("duckypunishment.punish")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /punish <player>");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            // Allow punishing offline players
            player.sendMessage("§eOpening punishment GUI for offline player: " + targetName);
        }

        mainGUI.openPunishmentGUI(player, targetName);
        return true;
    }
}