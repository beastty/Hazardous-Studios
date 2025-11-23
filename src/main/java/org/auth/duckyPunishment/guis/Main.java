package org.auth.duckyPunishment.guis;

import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class Main {

    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public Main(DatabaseManager databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    public void openPunishmentGUI(Player staff, String targetName) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8§lPunish: §c" + targetName);

        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        boolean isBanned = databaseManager.isBanned(targetUUID);
        boolean isMuted = databaseManager.isMuted(targetUUID);

        // Player Head
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(targetName));
        headMeta.setDisplayName("§e§l" + targetName);
        headMeta.setLore(Arrays.asList(
                "§7",
                isBanned ? "§c§lStatus: BANNED" : "§a§lStatus: Not Banned",
                isMuted ? "§e§lMuted: YES" : "§7§lMuted: NO",
                "§7",
                "§7Click to view history"
        ));
        head.setItemMeta(headMeta);
        gui.setItem(4, head);

        // Ban Button
        ItemStack banItem = new ItemStack(isBanned ? Material.RED_CONCRETE : Material.BARRIER);
        ItemMeta banMeta = banItem.getItemMeta();
        banMeta.setDisplayName(isBanned ? "§c§lUnban Player" : "§c§lBan Player");
        banMeta.setLore(Arrays.asList(
                "§7",
                isBanned ? "§7Click to unban this player" : "§7Permanently ban this player",
                "§7from the server",
                "§7",
                "§eLeft-Click: §fPermanent Ban",
                "§eRight-Click: §fTemporary Ban"
        ));
        banItem.setItemMeta(banMeta);
        gui.setItem(10, banItem);

        // Mute Button
        ItemStack muteItem = new ItemStack(isMuted ? Material.YELLOW_CONCRETE : Material.BOOK);
        ItemMeta muteMeta = muteItem.getItemMeta();
        muteMeta.setDisplayName(isMuted ? "§e§lUnmute Player" : "§e§lMute Player");
        muteMeta.setLore(Arrays.asList(
                "§7",
                isMuted ? "§7Click to unmute this player" : "§7Prevent this player from",
                "§7chatting on the server",
                "§7",
                "§eLeft-Click: §fPermanent Mute",
                "§eRight-Click: §fTemporary Mute"
        ));
        muteItem.setItemMeta(muteMeta);
        gui.setItem(12, muteItem);

        // Kick Button
        ItemStack kickItem = new ItemStack(Material.IRON_DOOR);
        ItemMeta kickMeta = kickItem.getItemMeta();
        kickMeta.setDisplayName("§6§lKick Player");
        kickMeta.setLore(Arrays.asList(
                "§7",
                "§7Remove this player from",
                "§7the server temporarily",
                "§7",
                "§eClick to kick"
        ));
        kickItem.setItemMeta(kickMeta);
        gui.setItem(14, kickItem);

        // Warn Button
        ItemStack warnItem = new ItemStack(Material.PAPER);
        ItemMeta warnMeta = warnItem.getItemMeta();
        warnMeta.setDisplayName("§d§lWarn Player");
        warnMeta.setLore(Arrays.asList(
                "§7",
                "§7Issue a formal warning",
                "§7to this player",
                "§7",
                "§eClick to warn"
        ));
        warnItem.setItemMeta(warnMeta);
        gui.setItem(16, warnItem);

        // History Button
        ItemStack historyItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta historyMeta = historyItem.getItemMeta();
        historyMeta.setDisplayName("§b§lPunishment History");
        historyMeta.setLore(Arrays.asList(
                "§7",
                "§7View all previous punishments",
                "§7issued to this player",
                "§7",
                "§eClick to view history"
        ));
        historyItem.setItemMeta(historyMeta);
        gui.setItem(22, historyItem);

        // Fill empty slots with glass panes
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        staff.openInventory(gui);
    }

    public void openHistoryGUI(Player staff, String targetName) {
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        DatabaseManager.PunishmentHistory history = databaseManager.getHistory(targetUUID);

        Inventory gui = Bukkit.createInventory(null, 54, "§8§lHistory: §e" + targetName);

        // Player head
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(targetName));
        headMeta.setDisplayName("§e§l" + targetName);
        headMeta.setLore(Arrays.asList(
                "§7",
                "§7Total Punishments: §e" + history.getTotalPunishments(),
                "§c  Bans: " + history.getBans().size(),
                "§e  Mutes: " + history.getMutes().size(),
                "§6  Kicks: " + history.getKicks().size(),
                "§d  Warnings: " + history.getWarnings().size()
        ));
        head.setItemMeta(headMeta);
        gui.setItem(4, head);

        // Bans button
        ItemStack bansItem = new ItemStack(Material.BARRIER);
        ItemMeta bansMeta = bansItem.getItemMeta();
        bansMeta.setDisplayName("§c§lBans (" + history.getBans().size() + ")");
        bansMeta.setLore(Arrays.asList("§7Click to view all bans"));
        bansItem.setItemMeta(bansMeta);
        gui.setItem(19, bansItem);

        // Mutes button
        ItemStack mutesItem = new ItemStack(Material.BOOK);
        ItemMeta mutesMeta = mutesItem.getItemMeta();
        mutesMeta.setDisplayName("§e§lMutes (" + history.getMutes().size() + ")");
        mutesMeta.setLore(Arrays.asList("§7Click to view all mutes"));
        mutesItem.setItemMeta(mutesMeta);
        gui.setItem(21, mutesItem);

        // Kicks button
        ItemStack kicksItem = new ItemStack(Material.IRON_DOOR);
        ItemMeta kicksMeta = kicksItem.getItemMeta();
        kicksMeta.setDisplayName("§6§lKicks (" + history.getKicks().size() + ")");
        kicksMeta.setLore(Arrays.asList("§7Click to view all kicks"));
        kicksItem.setItemMeta(kicksMeta);
        gui.setItem(23, kicksItem);

        // Warnings button
        ItemStack warnsItem = new ItemStack(Material.PAPER);
        ItemMeta warnsMeta = warnsItem.getItemMeta();
        warnsMeta.setDisplayName("§d§lWarnings (" + history.getWarnings().size() + ")");
        warnsMeta.setLore(Arrays.asList("§7Click to view all warnings"));
        warnsItem.setItemMeta(warnsMeta);
        gui.setItem(25, warnsItem);

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c§lBack");
        backMeta.setLore(Arrays.asList("§7Return to punishment menu"));
        back.setItemMeta(backMeta);
        gui.setItem(49, back);

        // Fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        staff.openInventory(gui);
    }

    public void openDetailedHistoryGUI(Player staff, String targetName, String type) {
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        DatabaseManager.PunishmentHistory history = databaseManager.getHistory(targetUUID);

        Inventory gui = Bukkit.createInventory(null, 54, "§8§l" + type + " History: §e" + targetName);
        int slot = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        switch (type.toLowerCase()) {
            case "bans":
                for (DatabaseManager.BanInfo ban : history.getBans()) {
                    if (slot >= 45) break;
                    ItemStack item = new ItemStack(Material.RED_CONCRETE);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§c§lBan #" + ban.id);
                    meta.setLore(Arrays.asList(
                            "§7Issued by: §f" + ban.issuerName,
                            "§7Reason: §f" + ban.reason,
                            "§7Date: §f" + dateFormat.format(new Date(ban.issuedAt)),
                            "§7Duration: §f" + (ban.expiresAt == -1 ? "Permanent" : "Temporary")
                    ));
                    item.setItemMeta(meta);
                    gui.setItem(slot++, item);
                }
                break;
            case "mutes":
                for (DatabaseManager.MuteInfo mute : history.getMutes()) {
                    if (slot >= 45) break;
                    ItemStack item = new ItemStack(Material.YELLOW_CONCRETE);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§e§lMute #" + mute.id);
                    meta.setLore(Arrays.asList(
                            "§7Issued by: §f" + mute.issuerName,
                            "§7Reason: §f" + mute.reason,
                            "§7Date: §f" + dateFormat.format(new Date(mute.issuedAt)),
                            "§7Duration: §f" + (mute.expiresAt == -1 ? "Permanent" : "Temporary")
                    ));
                    item.setItemMeta(meta);
                    gui.setItem(slot++, item);
                }
                break;
            case "kicks":
                for (DatabaseManager.KickInfo kick : history.getKicks()) {
                    if (slot >= 45) break;
                    ItemStack item = new ItemStack(Material.ORANGE_CONCRETE);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§6§lKick #" + kick.id);
                    meta.setLore(Arrays.asList(
                            "§7Issued by: §f" + kick.issuerName,
                            "§7Reason: §f" + kick.reason,
                            "§7Date: §f" + dateFormat.format(new Date(kick.issuedAt))
                    ));
                    item.setItemMeta(meta);
                    gui.setItem(slot++, item);
                }
                break;
            case "warnings":
                for (DatabaseManager.WarningInfo warn : history.getWarnings()) {
                    if (slot >= 45) break;
                    ItemStack item = new ItemStack(Material.PINK_CONCRETE);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§d§lWarning #" + warn.id);
                    meta.setLore(Arrays.asList(
                            "§7Issued by: §f" + warn.issuerName,
                            "§7Reason: §f" + warn.reason,
                            "§7Date: §f" + dateFormat.format(new Date(warn.issuedAt))
                    ));
                    item.setItemMeta(meta);
                    gui.setItem(slot++, item);
                }
                break;
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c§lBack");
        backMeta.setLore(Arrays.asList("§7Return to history menu"));
        back.setItemMeta(backMeta);
        gui.setItem(49, back);

        staff.openInventory(gui);
    }

    public void openDurationGUI(Player staff, String targetName, String punishmentType) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8§lSelect Duration: §e" + punishmentType);

        // Permanent
        ItemStack perm = createDurationItem(Material.RED_CONCRETE, "§c§lPermanent", "Never expires", "perm");
        gui.setItem(4, perm);

        // Presets
        ItemStack hour1 = createDurationItem(Material.LIGHT_BLUE_CONCRETE, "§b§l1 Hour", "Temporary punishment", "1h");
        ItemStack hours6 = createDurationItem(Material.CYAN_CONCRETE, "§3§l6 Hours", "Temporary punishment", "6h");
        ItemStack day1 = createDurationItem(Material.YELLOW_CONCRETE, "§e§l1 Day", "Temporary punishment", "1d");
        ItemStack days3 = createDurationItem(Material.ORANGE_CONCRETE, "§6§l3 Days", "Temporary punishment", "3d");
        ItemStack week1 = createDurationItem(Material.MAGENTA_CONCRETE, "§d§l1 Week", "Temporary punishment", "1w");
        ItemStack month1 = createDurationItem(Material.PURPLE_CONCRETE, "§5§l1 Month", "Temporary punishment", "30d");

        gui.setItem(10, hour1);
        gui.setItem(11, hours6);
        gui.setItem(12, day1);
        gui.setItem(14, days3);
        gui.setItem(15, week1);
        gui.setItem(16, month1);

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c§lBack");
        backMeta.setLore(Arrays.asList("§7Return to punishment menu"));
        back.setItemMeta(backMeta);
        gui.setItem(22, back);

        // Fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        staff.openInventory(gui);
    }

    public void openReasonGUI(Player staff, String targetName, String punishmentType, String duration) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8§lSelect Reason");

        // Common reasons
        ItemStack hacking = createReasonItem(Material.DIAMOND_SWORD, "§c§lHacking/Cheating", "hacking");
        ItemStack spam = createReasonItem(Material.BOOK, "§e§lSpam", "spam");
        ItemStack toxicity = createReasonItem(Material.FIRE_CHARGE, "§6§lToxic Behavior", "toxicity");
        ItemStack language = createReasonItem(Material.PAPER, "§d§lInappropriate Language", "language");
        ItemStack advertising = createReasonItem(Material.OAK_SIGN, "§b§lAdvertising", "advertising");
        ItemStack griefing = createReasonItem(Material.TNT, "§4§lGriefing", "griefing");
        ItemStack custom = createReasonItem(Material.WRITABLE_BOOK, "§a§lCustom Reason", "custom");

        gui.setItem(10, hacking);
        gui.setItem(11, spam);
        gui.setItem(12, toxicity);
        gui.setItem(13, language);
        gui.setItem(14, advertising);
        gui.setItem(15, griefing);
        gui.setItem(16, custom);

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c§lBack");
        backMeta.setLore(Arrays.asList("§7Return to duration menu"));
        back.setItemMeta(backMeta);
        gui.setItem(22, back);

        // Fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        staff.openInventory(gui);
    }

    private ItemStack createDurationItem(Material material, String name, String description, String duration) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(
                "§7",
                "§7" + description,
                "§7Duration: §f" + duration,
                "§7",
                "§eClick to select"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createReasonItem(Material material, String name, String reason) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(
                "§7",
                "§7Click to punish with",
                "§7this reason",
                "§7",
                "§eClick to select"
        ));
        item.setItemMeta(meta);
        return item;
    }
}