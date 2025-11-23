package org.auth.duckyPunishment.managers;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private Connection connection;
    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    public DatabaseManager(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false",
                    username, password
            );

            createTables();
            Bukkit.getLogger().info("[DuckyPunishment] Database connected successfully!");
        } catch (SQLException | ClassNotFoundException e) {
            Bukkit.getLogger().severe("[DuckyPunishment] Failed to connect to database!");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Bukkit.getLogger().info("[DuckyPunishment] Database disconnected!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() {
        try {
            Statement stmt = connection.createStatement();

            // Bans table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bans (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "player_name VARCHAR(16) NOT NULL," +
                    "issuer_uuid VARCHAR(36)," +
                    "issuer_name VARCHAR(16) NOT NULL," +
                    "reason TEXT NOT NULL," +
                    "issued_at BIGINT NOT NULL," +
                    "expires_at BIGINT," +
                    "active BOOLEAN DEFAULT TRUE," +
                    "unbanned_by VARCHAR(16)," +
                    "unbanned_at BIGINT," +
                    "unban_reason TEXT," +
                    "INDEX(uuid)," +
                    "INDEX(active)" +
                    ")");

            // Mutes table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS mutes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "player_name VARCHAR(16) NOT NULL," +
                    "issuer_uuid VARCHAR(36)," +
                    "issuer_name VARCHAR(16) NOT NULL," +
                    "reason TEXT NOT NULL," +
                    "issued_at BIGINT NOT NULL," +
                    "expires_at BIGINT," +
                    "active BOOLEAN DEFAULT TRUE," +
                    "unmuted_by VARCHAR(16)," +
                    "unmuted_at BIGINT," +
                    "unmute_reason TEXT," +
                    "INDEX(uuid)," +
                    "INDEX(active)" +
                    ")");

            // Kicks table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kicks (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "player_name VARCHAR(16) NOT NULL," +
                    "issuer_uuid VARCHAR(36)," +
                    "issuer_name VARCHAR(16) NOT NULL," +
                    "reason TEXT NOT NULL," +
                    "issued_at BIGINT NOT NULL," +
                    "INDEX(uuid)" +
                    ")");

            // Warnings table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS warnings (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "player_name VARCHAR(16) NOT NULL," +
                    "issuer_uuid VARCHAR(36)," +
                    "issuer_name VARCHAR(16) NOT NULL," +
                    "reason TEXT NOT NULL," +
                    "issued_at BIGINT NOT NULL," +
                    "acknowledged BOOLEAN DEFAULT FALSE," +
                    "INDEX(uuid)" +
                    ")");

            // IP Tracking table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ip_tracking (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "player_name VARCHAR(16) NOT NULL," +
                    "ip_address VARCHAR(45) NOT NULL," +
                    "first_seen BIGINT NOT NULL," +
                    "last_seen BIGINT NOT NULL," +
                    "UNIQUE KEY unique_player_ip (uuid, ip_address)," +
                    "INDEX(uuid)," +
                    "INDEX(ip_address)" +
                    ")");

            // IP Bans table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ip_bans (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "ip_address VARCHAR(45) NOT NULL UNIQUE," +
                    "issuer_uuid VARCHAR(36)," +
                    "issuer_name VARCHAR(16) NOT NULL," +
                    "reason TEXT NOT NULL," +
                    "issued_at BIGINT NOT NULL," +
                    "expires_at BIGINT," +
                    "active BOOLEAN DEFAULT TRUE," +
                    "unbanned_by VARCHAR(16)," +
                    "unbanned_at BIGINT," +
                    "INDEX(ip_address)," +
                    "INDEX(active)" +
                    ")");

            // IP Mutes table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ip_mutes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "ip_address VARCHAR(45) NOT NULL UNIQUE," +
                    "issuer_uuid VARCHAR(36)," +
                    "issuer_name VARCHAR(16) NOT NULL," +
                    "reason TEXT NOT NULL," +
                    "issued_at BIGINT NOT NULL," +
                    "expires_at BIGINT," +
                    "active BOOLEAN DEFAULT TRUE," +
                    "unmuted_by VARCHAR(16)," +
                    "unmuted_at BIGINT," +
                    "INDEX(ip_address)," +
                    "INDEX(active)" +
                    ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ban Methods
    public void addBan(UUID uuid, String playerName, UUID issuerUUID, String issuerName, String reason, long expiresAt) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO bans (uuid, player_name, issuer_uuid, issuer_name, reason, issued_at, expires_at, active) VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, playerName);
            ps.setString(3, issuerUUID != null ? issuerUUID.toString() : null);
            ps.setString(4, issuerName);
            ps.setString(5, reason);
            ps.setLong(6, System.currentTimeMillis());
            ps.setLong(7, expiresAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBanned(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM bans WHERE uuid = ? AND active = TRUE"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long expiresAt = rs.getLong("expires_at");
                if (expiresAt == -1 || expiresAt > System.currentTimeMillis()) {
                    return true;
                } else {
                    // Ban expired, deactivate it
                    deactivateBan(rs.getInt("id"));
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public BanInfo getActiveBan(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM bans WHERE uuid = ? AND active = TRUE"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long expiresAt = rs.getLong("expires_at");
                if (expiresAt == -1 || expiresAt > System.currentTimeMillis()) {
                    return new BanInfo(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("player_name"),
                            rs.getString("issuer_name"),
                            rs.getString("reason"),
                            rs.getLong("issued_at"),
                            expiresAt
                    );
                } else {
                    deactivateBan(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeBan(UUID uuid, String unbannerName, String reason) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE bans SET active = FALSE, unbanned_by = ?, unbanned_at = ?, unban_reason = ? WHERE uuid = ? AND active = TRUE"
            );
            ps.setString(1, unbannerName);
            ps.setLong(2, System.currentTimeMillis());
            ps.setString(3, reason);
            ps.setString(4, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deactivateBan(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE bans SET active = FALSE WHERE id = ?"
            );
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Mute Methods
    public void addMute(UUID uuid, String playerName, UUID issuerUUID, String issuerName, String reason, long expiresAt) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO mutes (uuid, player_name, issuer_uuid, issuer_name, reason, issued_at, expires_at, active) VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, playerName);
            ps.setString(3, issuerUUID != null ? issuerUUID.toString() : null);
            ps.setString(4, issuerName);
            ps.setString(5, reason);
            ps.setLong(6, System.currentTimeMillis());
            ps.setLong(7, expiresAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isMuted(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM mutes WHERE uuid = ? AND active = TRUE"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long expiresAt = rs.getLong("expires_at");
                if (expiresAt == -1 || expiresAt > System.currentTimeMillis()) {
                    return true;
                } else {
                    deactivateMute(rs.getInt("id"));
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public MuteInfo getActiveMute(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM mutes WHERE uuid = ? AND active = TRUE"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long expiresAt = rs.getLong("expires_at");
                if (expiresAt == -1 || expiresAt > System.currentTimeMillis()) {
                    return new MuteInfo(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("player_name"),
                            rs.getString("issuer_name"),
                            rs.getString("reason"),
                            rs.getLong("issued_at"),
                            expiresAt
                    );
                } else {
                    deactivateMute(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeMute(UUID uuid, String unmutterName, String reason) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE mutes SET active = FALSE, unmuted_by = ?, unmuted_at = ?, unmute_reason = ? WHERE uuid = ? AND active = TRUE"
            );
            ps.setString(1, unmutterName);
            ps.setLong(2, System.currentTimeMillis());
            ps.setString(3, reason);
            ps.setString(4, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deactivateMute(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE mutes SET active = FALSE WHERE id = ?"
            );
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Kick Methods
    public void addKick(UUID uuid, String playerName, UUID issuerUUID, String issuerName, String reason) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO kicks (uuid, player_name, issuer_uuid, issuer_name, reason, issued_at) VALUES (?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, playerName);
            ps.setString(3, issuerUUID != null ? issuerUUID.toString() : null);
            ps.setString(4, issuerName);
            ps.setString(5, reason);
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Warning Methods
    public int addWarning(UUID uuid, String playerName, UUID issuerUUID, String issuerName, String reason) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO warnings (uuid, player_name, issuer_uuid, issuer_name, reason, issued_at) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, playerName);
            ps.setString(3, issuerUUID != null ? issuerUUID.toString() : null);
            ps.setString(4, issuerName);
            ps.setString(5, reason);
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getWarningCount(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) FROM warnings WHERE uuid = ?"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // History Methods
    public PunishmentHistory getHistory(UUID uuid) {
        PunishmentHistory history = new PunishmentHistory();

        try {
            // Get bans
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM bans WHERE uuid = ? ORDER BY issued_at DESC"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                history.addBan(new BanInfo(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getString("issuer_name"),
                        rs.getString("reason"),
                        rs.getLong("issued_at"),
                        rs.getLong("expires_at")
                ));
            }

            // Get mutes
            ps = connection.prepareStatement(
                    "SELECT * FROM mutes WHERE uuid = ? ORDER BY issued_at DESC"
            );
            ps.setString(1, uuid.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                history.addMute(new MuteInfo(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getString("issuer_name"),
                        rs.getString("reason"),
                        rs.getLong("issued_at"),
                        rs.getLong("expires_at")
                ));
            }

            // Get kicks
            ps = connection.prepareStatement(
                    "SELECT * FROM kicks WHERE uuid = ? ORDER BY issued_at DESC"
            );
            ps.setString(1, uuid.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                history.addKick(new KickInfo(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getString("issuer_name"),
                        rs.getString("reason"),
                        rs.getLong("issued_at")
                ));
            }

            // Get warnings
            ps = connection.prepareStatement(
                    "SELECT * FROM warnings WHERE uuid = ? ORDER BY issued_at DESC"
            );
            ps.setString(1, uuid.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                history.addWarning(new WarningInfo(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getString("issuer_name"),
                        rs.getString("reason"),
                        rs.getLong("issued_at")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }

    // Clear Methods
    public void clearBans(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM bans WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearMutes(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM mutes WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearKicks(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM kicks WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearWarnings(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM warnings WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean clearBanById(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM bans WHERE id = ?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean clearMuteById(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM mutes WHERE id = ?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean clearKickById(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM kicks WHERE id = ?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean clearWarningById(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM warnings WHERE id = ?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Staff Activity Methods
    public StaffActivity getStaffActivity(UUID staffUUID, String staffName) {
        StaffActivity activity = new StaffActivity();

        try {
            // Get bans issued
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM bans WHERE issuer_name = ? ORDER BY issued_at DESC LIMIT 50"
            );
            ps.setString(1, staffName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                activity.addBan(new BanInfo(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getString("issuer_name"),
                        rs.getString("reason"),
                        rs.getLong("issued_at"),
                        rs.getLong("expires_at")
                ));
            }

            // Get mutes issued
            ps = connection.prepareStatement(
                    "SELECT * FROM mutes WHERE issuer_name = ? ORDER BY issued_at DESC LIMIT 50"
            );
            ps.setString(1, staffName);
            rs = ps.executeQuery();
            while (rs.next()) {
                activity.addMute(new MuteInfo(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getString("issuer_name"),
                        rs.getString("reason"),
                        rs.getLong("issued_at"),
                        rs.getLong("expires_at")
                ));
            }

            // Get kicks issued
            ps = connection.prepareStatement(
                    "SELECT * FROM kicks WHERE issuer_name = ? ORDER BY issued_at DESC LIMIT 50"
            );
            ps.setString(1, staffName);
            rs = ps.executeQuery();
            while (rs.next()) {
                activity.addKick(new KickInfo(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getString("issuer_name"),
                        rs.getString("reason"),
                        rs.getLong("issued_at")
                ));
            }

            // Get warnings issued
            ps = connection.prepareStatement(
                    "SELECT * FROM warnings WHERE issuer_name = ? ORDER BY issued_at DESC LIMIT 50"
            );
            ps.setString(1, staffName);
            rs = ps.executeQuery();
            while (rs.next()) {
                activity.addWarning(new WarningInfo(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getString("issuer_name"),
                        rs.getString("reason"),
                        rs.getLong("issued_at")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activity;
    }

    // IP Tracking Methods
    public void trackIP(UUID uuid, String playerName, String ipAddress) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO ip_tracking (uuid, player_name, ip_address, first_seen, last_seen) " +
                            "VALUES (?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE player_name = ?, last_seen = ?"
            );
            long now = System.currentTimeMillis();
            ps.setString(1, uuid.toString());
            ps.setString(2, playerName);
            ps.setString(3, ipAddress);
            ps.setLong(4, now);
            ps.setLong(5, now);
            ps.setString(6, playerName);
            ps.setLong(7, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getLastKnownIP(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT ip_address FROM ip_tracking WHERE uuid = ? ORDER BY last_seen DESC LIMIT 1"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("ip_address");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PlayerIPRecord> getAccountsByIP(String ipAddress) {
        List<PlayerIPRecord> accounts = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM ip_tracking WHERE ip_address = ? ORDER BY last_seen DESC"
            );
            ps.setString(1, ipAddress);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                accounts.add(new PlayerIPRecord(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getString("ip_address"),
                        rs.getLong("first_seen"),
                        rs.getLong("last_seen")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    // IP Ban Methods
    public void addIPBan(String ipAddress, UUID issuerUUID, String issuerName, String reason, long expiresAt) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO ip_bans (ip_address, issuer_uuid, issuer_name, reason, issued_at, expires_at, active) " +
                            "VALUES (?, ?, ?, ?, ?, ?, TRUE)"
            );
            ps.setString(1, ipAddress);
            ps.setString(2, issuerUUID != null ? issuerUUID.toString() : null);
            ps.setString(3, issuerName);
            ps.setString(4, reason);
            ps.setLong(5, System.currentTimeMillis());
            ps.setLong(6, expiresAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isIPBanned(String ipAddress) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM ip_bans WHERE ip_address = ? AND active = TRUE"
            );
            ps.setString(1, ipAddress);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long expiresAt = rs.getLong("expires_at");
                if (expiresAt == -1 || expiresAt > System.currentTimeMillis()) {
                    return true;
                } else {
                    deactivateIPBan(rs.getInt("id"));
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public IPBanInfo getIPBan(String ipAddress) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM ip_bans WHERE ip_address = ? AND active = TRUE"
            );
            ps.setString(1, ipAddress);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long expiresAt = rs.getLong("expires_at");
                if (expiresAt == -1 || expiresAt > System.currentTimeMillis()) {
                    return new IPBanInfo(
                            rs.getInt("id"),
                            rs.getString("ip_address"),
                            rs.getString("issuer_name"),
                            rs.getString("reason"),
                            rs.getLong("issued_at"),
                            expiresAt
                    );
                } else {
                    deactivateIPBan(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeIPBan(String ipAddress, String unbannerName) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE ip_bans SET active = FALSE, unbanned_by = ?, unbanned_at = ? WHERE ip_address = ? AND active = TRUE"
            );
            ps.setString(1, unbannerName);
            ps.setLong(2, System.currentTimeMillis());
            ps.setString(3, ipAddress);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deactivateIPBan(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE ip_bans SET active = FALSE WHERE id = ?"
            );
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // IP Mute Methods
    public void addIPMute(String ipAddress, UUID issuerUUID, String issuerName, String reason, long expiresAt) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO ip_mutes (ip_address, issuer_uuid, issuer_name, reason, issued_at, expires_at, active) " +
                            "VALUES (?, ?, ?, ?, ?, ?, TRUE)"
            );
            ps.setString(1, ipAddress);
            ps.setString(2, issuerUUID != null ? issuerUUID.toString() : null);
            ps.setString(3, issuerName);
            ps.setString(4, reason);
            ps.setLong(5, System.currentTimeMillis());
            ps.setLong(6, expiresAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isIPMuted(String ipAddress) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM ip_mutes WHERE ip_address = ? AND active = TRUE"
            );
            ps.setString(1, ipAddress);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long expiresAt = rs.getLong("expires_at");
                if (expiresAt == -1 || expiresAt > System.currentTimeMillis()) {
                    return true;
                } else {
                    deactivateIPMute(rs.getInt("id"));
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public IPMuteInfo getIPMute(String ipAddress) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM ip_mutes WHERE ip_address = ? AND active = TRUE"
            );
            ps.setString(1, ipAddress);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long expiresAt = rs.getLong("expires_at");
                if (expiresAt == -1 || expiresAt > System.currentTimeMillis()) {
                    return new IPMuteInfo(
                            rs.getInt("id"),
                            rs.getString("ip_address"),
                            rs.getString("issuer_name"),
                            rs.getString("reason"),
                            rs.getLong("issued_at"),
                            expiresAt
                    );
                } else {
                    deactivateIPMute(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeIPMute(String ipAddress, String unmutterName) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE ip_mutes SET active = FALSE, unmuted_by = ?, unmuted_at = ? WHERE ip_address = ? AND active = TRUE"
            );
            ps.setString(1, unmutterName);
            ps.setLong(2, System.currentTimeMillis());
            ps.setString(3, ipAddress);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deactivateIPMute(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE ip_mutes SET active = FALSE WHERE id = ?"
            );
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Inner classes for data structures
    public static class BanInfo {
        public int id;
        public UUID uuid;
        public String playerName;
        public String issuerName;
        public String reason;
        public long issuedAt;
        public long expiresAt;

        public BanInfo(int id, UUID uuid, String playerName, String issuerName, String reason, long issuedAt, long expiresAt) {
            this.id = id;
            this.uuid = uuid;
            this.playerName = playerName;
            this.issuerName = issuerName;
            this.reason = reason;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
        }
    }

    public static class MuteInfo {
        public int id;
        public UUID uuid;
        public String playerName;
        public String issuerName;
        public String reason;
        public long issuedAt;
        public long expiresAt;

        public MuteInfo(int id, UUID uuid, String playerName, String issuerName, String reason, long issuedAt, long expiresAt) {
            this.id = id;
            this.uuid = uuid;
            this.playerName = playerName;
            this.issuerName = issuerName;
            this.reason = reason;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
        }
    }

    public static class KickInfo {
        public int id;
        public UUID uuid;
        public String playerName;
        public String issuerName;
        public String reason;
        public long issuedAt;

        public KickInfo(int id, UUID uuid, String playerName, String issuerName, String reason, long issuedAt) {
            this.id = id;
            this.uuid = uuid;
            this.playerName = playerName;
            this.issuerName = issuerName;
            this.reason = reason;
            this.issuedAt = issuedAt;
        }
    }

    public static class WarningInfo {
        public int id;
        public UUID uuid;
        public String playerName;
        public String issuerName;
        public String reason;
        public long issuedAt;

        public WarningInfo(int id, UUID uuid, String playerName, String issuerName, String reason, long issuedAt) {
            this.id = id;
            this.uuid = uuid;
            this.playerName = playerName;
            this.issuerName = issuerName;
            this.reason = reason;
            this.issuedAt = issuedAt;
        }
    }

    public static class PunishmentHistory {
        private List<BanInfo> bans = new ArrayList<>();
        private List<MuteInfo> mutes = new ArrayList<>();
        private List<KickInfo> kicks = new ArrayList<>();
        private List<WarningInfo> warnings = new ArrayList<>();

        public void addBan(BanInfo ban) { bans.add(ban); }
        public void addMute(MuteInfo mute) { mutes.add(mute); }
        public void addKick(KickInfo kick) { kicks.add(kick); }
        public void addWarning(WarningInfo warning) { warnings.add(warning); }

        public List<BanInfo> getBans() { return bans; }
        public List<MuteInfo> getMutes() { return mutes; }
        public List<KickInfo> getKicks() { return kicks; }
        public List<WarningInfo> getWarnings() { return warnings; }

        public int getTotalPunishments() {
            return bans.size() + mutes.size() + kicks.size() + warnings.size();
        }
    }

    public static class StaffActivity {
        private List<BanInfo> bansIssued = new ArrayList<>();
        private List<MuteInfo> mutesIssued = new ArrayList<>();
        private List<KickInfo> kicksIssued = new ArrayList<>();
        private List<WarningInfo> warningsIssued = new ArrayList<>();

        public void addBan(BanInfo ban) { bansIssued.add(ban); }
        public void addMute(MuteInfo mute) { mutesIssued.add(mute); }
        public void addKick(KickInfo kick) { kicksIssued.add(kick); }
        public void addWarning(WarningInfo warning) { warningsIssued.add(warning); }

        public List<BanInfo> getBansIssued() { return bansIssued; }
        public List<MuteInfo> getMutesIssued() { return mutesIssued; }
        public List<KickInfo> getKicksIssued() { return kicksIssued; }
        public List<WarningInfo> getWarningsIssued() { return warningsIssued; }

        public int getTotalPunishments() {
            return bansIssued.size() + mutesIssued.size() + kicksIssued.size() + warningsIssued.size();
        }
    }

    public static class PlayerIPRecord {
        public UUID uuid;
        public String playerName;
        public String ipAddress;
        public long firstSeen;
        public long lastSeen;

        public PlayerIPRecord(UUID uuid, String playerName, String ipAddress, long firstSeen, long lastSeen) {
            this.uuid = uuid;
            this.playerName = playerName;
            this.ipAddress = ipAddress;
            this.firstSeen = firstSeen;
            this.lastSeen = lastSeen;
        }
    }

    public static class IPBanInfo {
        public int id;
        public String ipAddress;
        public String issuerName;
        public String reason;
        public long issuedAt;
        public long expiresAt;

        public IPBanInfo(int id, String ipAddress, String issuerName, String reason, long issuedAt, long expiresAt) {
            this.id = id;
            this.ipAddress = ipAddress;
            this.issuerName = issuerName;
            this.reason = reason;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
        }
    }

    public static class IPMuteInfo {
        public int id;
        public String ipAddress;
        public String issuerName;
        public String reason;
        public long issuedAt;
        public long expiresAt;

        public IPMuteInfo(int id, String ipAddress, String issuerName, String reason, long issuedAt, long expiresAt) {
            this.id = id;
            this.ipAddress = ipAddress;
            this.issuerName = issuerName;
            this.reason = reason;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
        }
    }
}