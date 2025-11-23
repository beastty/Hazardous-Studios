package org.auth.duckyPunishment.listeners;

import org.auth.duckyPunishment.guis.Main;
import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIListener implements Listener {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;
    private final Main mainGUI;

    // Store GUI context for players
    private final Map<UUID, GUIContext> guiContext = new HashMap<>();

    public GUIListener(DatabaseManager databaseManager, ConfigManager configManager, Main mainGUI) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
        this.mainGUI = mainGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        // Check if it's one of our GUIs
        if (!title.contains("Punish:") && !title.contains("Select Duration") &&
                !title.contains("Select Reason") && !title.contains("History:")) {
            return;
        }

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String itemName = clicked.getItemMeta().getDisplayName();

        // Handle main punishment GUI
        if (title.contains("Punish:")) {
            handleMainGUI(player, title, itemName, e.isLeftClick());
        }
        // Handle duration selection GUI
        else if (title.contains("Select Duration")) {
            handleDurationGUI(player, title, itemName);
        }
        // Handle reason selection GUI
        else if (title.contains("Select Reason")) {
            handleReasonGUI(player, title, itemName);
        }
        // Handle history GUI
        else if (title.contains("History:")) {
            handleReasonGUI(player, title, itemName);
        }
    }

    private void handleMainGUI(Player staff, String title, String itemName, boolean isLeftClick) {
        String targetName = title.split("§c")[1];
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        if (itemName.contains("Ban Player")) {
            if (isLeftClick) {
                // Permanent ban - go to reason selection
                GUIContext context = new GUIContext(targetName, "ban", "perm");
                guiContext.put(staff.getUniqueId(), context);
                mainGUI.openReasonGUI(staff, targetName, "ban", "perm");
            } else {
                // Temporary ban - go to duration selection
                GUIContext context = new GUIContext(targetName, "ban", null);
                guiContext.put(staff.getUniqueId(), context);
                mainGUI.openDurationGUI(staff, targetName, "Ban");
            }
        }
        else if (itemName.contains("Unban Player")) {
            databaseManager.removeBan(targetUUID, staff.getName(), "Unbanned via GUI");
            staff.sendMessage("§aSuccessfully unbanned " + targetName);
            staff.closeInventory();
        }
        else if (itemName.contains("Mute Player")) {
            if (isLeftClick) {
                // Permanent mute
                GUIContext context = new GUIContext(targetName, "mute", "perm");
                guiContext.put(staff.getUniqueId(), context);
                mainGUI.openReasonGUI(staff, targetName, "mute", "perm");
            } else {
                // Temporary mute
                GUIContext context = new GUIContext(targetName, "mute", null);
                guiContext.put(staff.getUniqueId(), context);
                mainGUI.openDurationGUI(staff, targetName, "Mute");
            }
        }
        else if (itemName.contains("Unmute Player")) {
            databaseManager.removeMute(targetUUID, staff.getName(), "Unmuted via GUI");
            staff.sendMessage("§aSuccessfully unmuted " + targetName);
            staff.closeInventory();
        }
        else if (itemName.contains("Kick Player")) {
            GUIContext context = new GUIContext(targetName, "kick", null);
            guiContext.put(staff.getUniqueId(), context);
            mainGUI.openReasonGUI(staff, targetName, "kick", null);
        }
        else if (itemName.contains("Warn Player")) {
            GUIContext context = new GUIContext(targetName, "warn", null);
            guiContext.put(staff.getUniqueId(), context);
            mainGUI.openReasonGUI(staff, targetName, "warn", null);
        }
        else if (itemName.contains("Punishment History")) {
            staff.closeInventory();
            staff.performCommand("history " + targetName);
        }
    }

    private void handleDurationGUI(Player staff, String title, String itemName) {
        GUIContext context = guiContext.get(staff.getUniqueId());
        if (context == null) return;

        if (itemName.contains("Back")) {
            mainGUI.openPunishmentGUI(staff, context.targetName);
            return;
        }

        // Parse duration from item name
        String duration = parseDurationFromName(itemName);
        context.duration = duration;

        mainGUI.openReasonGUI(staff, context.targetName, context.punishmentType, duration);
    }

    private void handleReasonGUI(Player staff, String title, String itemName) {
        GUIContext context = guiContext.get(staff.getUniqueId());
        if (context == null) return;

        if (itemName.contains("Back")) {
            if (context.duration != null && !context.duration.equals("perm")) {
                mainGUI.openDurationGUI(staff, context.targetName, context.punishmentType);
            } else {
                mainGUI.openPunishmentGUI(staff, context.targetName);
            }
            return;
        }

        String reason = parseReasonFromName(itemName);

        if (reason.equals("custom")) {
            staff.closeInventory();
            staff.sendMessage("§eType the custom reason in chat:");
            // Would need a chat listener to handle custom reason
            return;
        }

        // Execute punishment
        executePunishment(staff, context, reason);
        guiContext.remove(staff.getUniqueId());
    }

    private void executePunishment(Player staff, GUIContext context, String reason) {
        String targetName = context.targetName;
        String type = context.punishmentType;
        String duration = context.duration != null ? context.duration : "perm";

        StringBuilder command = new StringBuilder();

        switch (type) {
            case "ban":
                if (duration.equals("perm")) {
                    command.append("ban ");
                } else {
                    command.append("tempban ");
                }
                command.append(targetName).append(" ");
                if (!duration.equals("perm")) {
                    command.append(duration).append(" ");
                }
                command.append(reason);
                break;

            case "mute":
                if (duration.equals("perm")) {
                    command.append("mute ");
                } else {
                    command.append("tempmute ");
                }
                command.append(targetName).append(" ");
                if (!duration.equals("perm")) {
                    command.append(duration).append(" ");
                }
                command.append(reason);
                break;

            case "kick":
                command.append("kick ").append(targetName).append(" ").append(reason);
                break;

            case "warn":
                // Would need warn command implementation
                staff.sendMessage("§cWarn command not yet implemented!");
                staff.closeInventory();
                return;
        }

        staff.closeInventory();
        staff.performCommand(command.toString());
    }

    private String parseDurationFromName(String name) {
        if (name.contains("Permanent")) return "perm";
        if (name.contains("1 Hour")) return "1h";
        if (name.contains("6 Hours")) return "6h";
        if (name.contains("1 Day")) return "1d";
        if (name.contains("3 Days")) return "3d";
        if (name.contains("1 Week")) return "1w";
        if (name.contains("1 Month")) return "30d";
        return "perm";
    }

    private String parseReasonFromName(String name) {
        if (name.contains("Hacking")) return "Hacking/Cheating";
        if (name.contains("Spam")) return "Spam";
        if (name.contains("Toxic")) return "Toxic Behavior";
        if (name.contains("Language")) return "Inappropriate Language";
        if (name.contains("Advertising")) return "Advertising";
        if (name.contains("Griefing")) return "Griefing";
        if (name.contains("Custom")) return "custom";
        return "No reason specified";
    }

    // Context class to store GUI state
    private static class GUIContext {
        String targetName;
        String punishmentType;
        String duration;

        GUIContext(String targetName, String punishmentType, String duration) {
            this.targetName = targetName;
            this.punishmentType = punishmentType;
            this.duration = duration;
        }
    }
}