package org.auth.duckyPunishment.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.auth.duckyPunishment.managers.DatabaseManager;
import org.auth.duckyPunishment.managers.DatabaseManager.PunishmentHistory;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class WebServerManager {

    private HttpServer server;
    private final DatabaseManager databaseManager;
    private final int port;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public WebServerManager(DatabaseManager databaseManager, int port) {
        this.databaseManager = databaseManager;
        this.port = port;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // Register endpoints
            server.createContext("/", new HomeHandler());
            server.createContext("/bans", new BansHandler());
            server.createContext("/mutes", new MutesHandler());
            server.createContext("/history", new HistoryHandler());
            server.createContext("/api/punishments", new APIHandler());

            server.setExecutor(null);
            server.start();

            Bukkit.getLogger().info("[DuckyPunishment] Web server started on port " + port);
            Bukkit.getLogger().info("[DuckyPunishment] Access at: http://localhost:" + port);
        } catch (IOException e) {
            Bukkit.getLogger().severe("[DuckyPunishment] Failed to start web server!");
            e.printStackTrace();
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            Bukkit.getLogger().info("[DuckyPunishment] Web server stopped!");
        }
    }

    // Home Page Handler
    class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = generateHomePage();
            sendResponse(exchange, html);
        }
    }

    // Bans List Handler
    class BansHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = generateBansPage();
            sendResponse(exchange, html);
        }
    }

    // Mutes List Handler
    class MutesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = generateMutesPage();
            sendResponse(exchange, html);
        }
    }

    // History Lookup Handler
    class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String playerName = null;

            if (query != null && query.startsWith("player=")) {
                playerName = query.substring(7).replace("+", " ");
            }

            String html = generateHistoryPage(playerName);
            sendResponse(exchange, html);
        }
    }

    // API Handler (JSON responses)
    class APIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = generateAPIResponse();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(json.getBytes());
            os.close();
        }
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private String generateHomePage() {
        int activeBans = getActiveBansCount();
        int activeMutes = getActiveMutesCount();
        int totalPunishments = getTotalPunishmentsCount();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<title>DuckyPunishment - Home</title>");
        html.append(getCSS());
        html.append("</head><body>");
        html.append(getHeader());
        html.append("<div class='container'>");
        html.append("<h1>Welcome to DuckyPunishment</h1>");
        html.append("<p>View and manage server punishments</p>");
        html.append("<div class='card-container'>");
        html.append("<div class='card'><h3>Active Bans</h3><p class='stat'>").append(activeBans).append("</p><a href='/bans'>View All Bans</a></div>");
        html.append("<div class='card'><h3>Active Mutes</h3><p class='stat'>").append(activeMutes).append("</p><a href='/mutes'>View All Mutes</a></div>");
        html.append("<div class='card'><h3>Total Punishments</h3><p class='stat'>").append(totalPunishments).append("</p><a href='/history'>Lookup Player</a></div>");
        html.append("</div></div>");
        html.append(getFooter());
        html.append("</body></html>");
        return html.toString();
    }

    private String generateBansPage() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<title>DuckyPunishment - Bans</title>");
        html.append(getCSS());
        html.append("</head><body>");
        html.append(getHeader());
        html.append("<div class='container'>");
        html.append("<h1>Active Bans</h1>");
        html.append("<table>");
        html.append("<tr><th>Player</th><th>Banned By</th><th>Reason</th><th>Date</th><th>Expires</th></tr>");

        // Query active bans
        try {
            Connection conn = databaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM bans WHERE active = TRUE ORDER BY issued_at DESC LIMIT 100"
            );
            ResultSet rs = ps.executeQuery();

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String playerName = rs.getString("player_name");
                String issuerName = rs.getString("issuer_name");
                String reason = rs.getString("reason");
                long issuedAt = rs.getLong("issued_at");
                long expiresAt = rs.getLong("expires_at");

                html.append("<tr>");
                html.append("<td>").append(escapeHtml(playerName)).append("</td>");
                html.append("<td>").append(escapeHtml(issuerName)).append("</td>");
                html.append("<td>").append(escapeHtml(reason)).append("</td>");
                html.append("<td>").append(dateFormat.format(new Date(issuedAt))).append("</td>");
                html.append("<td>").append(expiresAt == -1 ? "Permanent" : dateFormat.format(new Date(expiresAt))).append("</td>");
                html.append("</tr>");
            }

            if (!hasResults) {
                html.append("<tr><td colspan='5' style='text-align:center'>No active bans found</td></tr>");
            }
        } catch (SQLException e) {
            html.append("<tr><td colspan='5'>Error loading bans</td></tr>");
            e.printStackTrace();
        }

        html.append("</table>");
        html.append("</div>");
        html.append(getFooter());
        html.append("</body></html>");
        return html.toString();
    }

    private String generateMutesPage() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<title>DuckyPunishment - Mutes</title>");
        html.append(getCSS());
        html.append("</head><body>");
        html.append(getHeader());
        html.append("<div class='container'>");
        html.append("<h1>Active Mutes</h1>");
        html.append("<table>");
        html.append("<tr><th>Player</th><th>Muted By</th><th>Reason</th><th>Date</th><th>Expires</th></tr>");

        try {
            Connection conn = databaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM mutes WHERE active = TRUE ORDER BY issued_at DESC LIMIT 100"
            );
            ResultSet rs = ps.executeQuery();

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String playerName = rs.getString("player_name");
                String issuerName = rs.getString("issuer_name");
                String reason = rs.getString("reason");
                long issuedAt = rs.getLong("issued_at");
                long expiresAt = rs.getLong("expires_at");

                html.append("<tr>");
                html.append("<td>").append(escapeHtml(playerName)).append("</td>");
                html.append("<td>").append(escapeHtml(issuerName)).append("</td>");
                html.append("<td>").append(escapeHtml(reason)).append("</td>");
                html.append("<td>").append(dateFormat.format(new Date(issuedAt))).append("</td>");
                html.append("<td>").append(expiresAt == -1 ? "Permanent" : dateFormat.format(new Date(expiresAt))).append("</td>");
                html.append("</tr>");
            }

            if (!hasResults) {
                html.append("<tr><td colspan='5' style='text-align:center'>No active mutes found</td></tr>");
            }
        } catch (SQLException e) {
            html.append("<tr><td colspan='5'>Error loading mutes</td></tr>");
            e.printStackTrace();
        }

        html.append("</table>");
        html.append("</div>");
        html.append(getFooter());
        html.append("</body></html>");
        return html.toString();
    }

    private String generateHistoryPage(String playerName) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<title>DuckyPunishment - History</title>");
        html.append(getCSS());
        html.append("</head><body>");
        html.append(getHeader());
        html.append("<div class='container'>");
        html.append("<h1>Player History</h1>");
        html.append("<form method='GET' action='/history'>");
        html.append("<input type='text' name='player' placeholder='Enter player name' value='").append(playerName != null ? escapeHtml(playerName) : "").append("'>");
        html.append("<button type='submit'>Search</button>");
        html.append("</form>");

        if (playerName != null && !playerName.isEmpty()) {
            UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            PunishmentHistory history = databaseManager.getHistory(uuid);

            html.append("<h2>History for: ").append(escapeHtml(playerName)).append("</h2>");
            html.append("<p>Total Punishments: ").append(history.getTotalPunishments()).append("</p>");

            // Bans
            if (!history.getBans().isEmpty()) {
                html.append("<h3>Bans (").append(history.getBans().size()).append(")</h3>");
                html.append("<table>");
                html.append("<tr><th>Issued By</th><th>Reason</th><th>Date</th><th>Duration</th></tr>");
                for (DatabaseManager.BanInfo ban : history.getBans()) {
                    html.append("<tr>");
                    html.append("<td>").append(escapeHtml(ban.issuerName)).append("</td>");
                    html.append("<td>").append(escapeHtml(ban.reason)).append("</td>");
                    html.append("<td>").append(dateFormat.format(new Date(ban.issuedAt))).append("</td>");
                    html.append("<td>").append(ban.expiresAt == -1 ? "Permanent" : "Temporary").append("</td>");
                    html.append("</tr>");
                }
                html.append("</table>");
            }

            // Mutes
            if (!history.getMutes().isEmpty()) {
                html.append("<h3>Mutes (").append(history.getMutes().size()).append(")</h3>");
                html.append("<table>");
                html.append("<tr><th>Issued By</th><th>Reason</th><th>Date</th><th>Duration</th></tr>");
                for (DatabaseManager.MuteInfo mute : history.getMutes()) {
                    html.append("<tr>");
                    html.append("<td>").append(escapeHtml(mute.issuerName)).append("</td>");
                    html.append("<td>").append(escapeHtml(mute.reason)).append("</td>");
                    html.append("<td>").append(dateFormat.format(new Date(mute.issuedAt))).append("</td>");
                    html.append("<td>").append(mute.expiresAt == -1 ? "Permanent" : "Temporary").append("</td>");
                    html.append("</tr>");
                }
                html.append("</table>");
            }

            // Kicks
            if (!history.getKicks().isEmpty()) {
                html.append("<h3>Kicks (").append(history.getKicks().size()).append(")</h3>");
                html.append("<table>");
                html.append("<tr><th>Issued By</th><th>Reason</th><th>Date</th></tr>");
                for (DatabaseManager.KickInfo kick : history.getKicks()) {
                    html.append("<tr>");
                    html.append("<td>").append(escapeHtml(kick.issuerName)).append("</td>");
                    html.append("<td>").append(escapeHtml(kick.reason)).append("</td>");
                    html.append("<td>").append(dateFormat.format(new Date(kick.issuedAt))).append("</td>");
                    html.append("</tr>");
                }
                html.append("</table>");
            }

            // Warnings
            if (!history.getWarnings().isEmpty()) {
                html.append("<h3>Warnings (").append(history.getWarnings().size()).append(")</h3>");
                html.append("<table>");
                html.append("<tr><th>Issued By</th><th>Reason</th><th>Date</th></tr>");
                for (DatabaseManager.WarningInfo warn : history.getWarnings()) {
                    html.append("<tr>");
                    html.append("<td>").append(escapeHtml(warn.issuerName)).append("</td>");
                    html.append("<td>").append(escapeHtml(warn.reason)).append("</td>");
                    html.append("<td>").append(dateFormat.format(new Date(warn.issuedAt))).append("</td>");
                    html.append("</tr>");
                }
                html.append("</table>");
            }

            if (history.getTotalPunishments() == 0) {
                html.append("<p>No punishment history found for this player.</p>");
            }
        }

        html.append("</div>");
        html.append(getFooter());
        html.append("</body></html>");
        return html.toString();
    }

    private String generateAPIResponse() {
        int activeBans = getActiveBansCount();
        int activeMutes = getActiveMutesCount();
        int totalPunishments = getTotalPunishmentsCount();

        return String.format(
                "{\"status\":\"ok\",\"data\":{\"active_bans\":%d,\"active_mutes\":%d,\"total_punishments\":%d}}",
                activeBans, activeMutes, totalPunishments
        );
    }

    private int getActiveBansCount() {
        try {
            Connection conn = databaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM bans WHERE active = TRUE");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getActiveMutesCount() {
        try {
            Connection conn = databaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM mutes WHERE active = TRUE");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalPunishmentsCount() {
        try {
            Connection conn = databaseManager.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT (SELECT COUNT(*) FROM bans) + " +
                            "(SELECT COUNT(*) FROM mutes) + " +
                            "(SELECT COUNT(*) FROM kicks) + " +
                            "(SELECT COUNT(*) FROM warnings) AS total"
            );
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String getHeader() {
        return "<div class='header'><h2>🦆 DuckyPunishment</h2><nav>" +
                "<a href='/'>Home</a><a href='/bans'>Bans</a><a href='/mutes'>Mutes</a><a href='/history'>History</a>" +
                "</nav></div>";
    }

    private String getFooter() {
        return "<div class='footer'><p>&copy; 2025 DuckyPunishment | Powered by DuckyPunishment</p></div>";
    }

    private String getCSS() {
        return "<style>" +
                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%); color: #fff; min-height: 100vh; }" +
                ".header { background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%); padding: 30px 20px; text-align: center; box-shadow: 0 4px 20px rgba(255,107,107,0.3); }" +
                ".header h2 { margin: 0; color: #fff; font-size: 2.5em; text-shadow: 2px 2px 4px rgba(0,0,0,0.3); }" +
                ".header nav { margin-top: 20px; }" +
                ".header nav a { color: #fff; text-decoration: none; margin: 0 20px; font-weight: bold; font-size: 1.1em; padding: 10px 20px; border-radius: 25px; transition: all 0.3s ease; display: inline-block; }" +
                ".header nav a:hover { background: rgba(255,255,255,0.2); transform: translateY(-2px); }" +
                ".container { max-width: 1400px; margin: 40px auto; padding: 20px; }" +
                "h1 { color: #ff6b6b; font-size: 2.5em; margin-bottom: 10px; text-shadow: 2px 2px 4px rgba(0,0,0,0.3); }" +
                ".card-container { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 30px; margin-top: 30px; }" +
                ".card { background: linear-gradient(135deg, #2a2a2a 0%, #333 100%); padding: 40px; border-radius: 20px; text-align: center; box-shadow: 0 10px 30px rgba(0,0,0,0.5); transition: all 0.3s ease; border: 1px solid rgba(255,107,107,0.2); }" +
                ".card:hover { transform: translateY(-10px); box-shadow: 0 15px 40px rgba(255,107,107,0.4); border-color: #ff6b6b; }" +
                ".card h3 { color: #ff6b6b; margin-top: 0; font-size: 1.8em; }" +
                ".card .stat { font-size: 3.5em; font-weight: bold; background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin: 30px 0; text-shadow: none; }" +
                ".card a { display: inline-block; margin-top: 20px; padding: 15px 35px; background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%); color: #fff; text-decoration: none; border-radius: 30px; font-weight: bold; transition: all 0.3s ease; box-shadow: 0 5px 15px rgba(255,107,107,0.3); }" +
                ".card a:hover { transform: scale(1.05); box-shadow: 0 8px 25px rgba(255,107,107,0.5); }" +
                "table { width: 100%; border-collapse: collapse; margin-top: 30px; background: #2a2a2a; border-radius: 15px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }" +
                "th, td { padding: 20px; text-align: left; border-bottom: 1px solid #3a3a3a; }" +
                "th { background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%); color: #fff; font-size: 1.1em; text-transform: uppercase; letter-spacing: 1px; }" +
                "tr:hover { background: #333; }" +
                "tr:last-child td { border-bottom: none; }" +
                "form { margin: 30px 0; display: flex; gap: 15px; flex-wrap: wrap; }" +
                "input[type='text'] { padding: 15px 20px; flex: 1; min-width: 250px; border: 2px solid #3a3a3a; border-radius: 30px; background: #2a2a2a; color: #fff; font-size: 1.1em; transition: all 0.3s ease; }" +
                "input[type='text']:focus { outline: none; border-color: #ff6b6b; box-shadow: 0 0 15px rgba(255,107,107,0.3); }" +
                "button { padding: 15px 40px; background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%); color: #fff; border: none; border-radius: 30px; cursor: pointer; font-size: 1.1em; font-weight: bold; transition: all 0.3s ease; box-shadow: 0 5px 15px rgba(255,107,107,0.3); }" +
                "button:hover { transform: scale(1.05); box-shadow: 0 8px 25px rgba(255,107,107,0.5); }" +
                ".footer { background: #2a2a2a; text-align: center; padding: 30px; margin-top: 60px; color: #888; border-top: 1px solid #3a3a3a; }" +
                ".footer p { font-size: 1.1em; }" +
                ".badge { display: inline-block; padding: 5px 12px; border-radius: 15px; font-size: 0.85em; font-weight: bold; margin-left: 8px; }" +
                ".badge-perm { background: #e74c3c; color: #fff; }" +
                ".badge-temp { background: #f39c12; color: #fff; }" +
                ".badge-active { background: #27ae60; color: #fff; }" +
                ".stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 30px 0; }" +
                ".stat-box { background: #2a2a2a; padding: 20px; border-radius: 15px; text-align: center; border: 1px solid #3a3a3a; }" +
                ".stat-box .number { font-size: 2.5em; color: #4ecdc4; font-weight: bold; }" +
                ".stat-box .label { color: #888; margin-top: 10px; }" +
                "@media (max-width: 768px) {" +
                "  .header h2 { font-size: 1.8em; }" +
                "  .header nav a { margin: 5px 10px; font-size: 0.9em; }" +
                "  .card-container { grid-template-columns: 1fr; }" +
                "  h1 { font-size: 1.8em; }" +
                "  table { font-size: 0.9em; }" +
                "  th, td { padding: 12px; }" +
                "}" +
                "</style>";
    }
}