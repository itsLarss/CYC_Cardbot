package dev.itsLarss.database;

import java.sql.*;
import java.util.*;

/**
 * Verwaltet Server-spezifische Einstellungen
 * - Erlaubte Channels
 * - NSFW Mode (nur in NSFW-Channels)
 */
public class ServerSettingsManager {

    private Connection connection;

    public ServerSettingsManager(Connection connection) {
        this.connection = connection;
        initializeTables();
    }

    private void initializeTables() {
        try {
            Statement stmt = connection.createStatement();

            // Server Settings Tabelle
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS server_settings (" +
                            "guild_id TEXT PRIMARY KEY, " +
                            "nsfw_enabled INTEGER DEFAULT 0, " +  // 0 = nur SFW, 1 = NSFW erlaubt
                            "setup_complete INTEGER DEFAULT 0, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            // Erlaubte Channels pro Server
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS allowed_channels (" +
                            "guild_id TEXT, " +
                            "channel_id TEXT, " +
                            "PRIMARY KEY (guild_id, channel_id))"
            );

            stmt.close();
            System.out.println("✅ Server Settings Tabellen erstellt!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== CHANNEL MANAGEMENT ====================

    /**
     * Fügt einen erlaubten Channel hinzu
     */
    public void addAllowedChannel(String guildId, String channelId) {
        try {
            String sql = "INSERT OR IGNORE INTO allowed_channels (guild_id, channel_id) VALUES (?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, guildId);
            pstmt.setString(2, channelId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Entfernt einen erlaubten Channel
     */
    public void removeAllowedChannel(String guildId, String channelId) {
        try {
            String sql = "DELETE FROM allowed_channels WHERE guild_id = ? AND channel_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, guildId);
            pstmt.setString(2, channelId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gibt alle erlaubten Channels zurück
     */
    public List<String> getAllowedChannels(String guildId) {
        List<String> channels = new ArrayList<>();
        try {
            String sql = "SELECT channel_id FROM allowed_channels WHERE guild_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                channels.add(rs.getString("channel_id"));
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return channels;
    }

    /**
     * Prüft ob ein Channel erlaubt ist
     * Wenn keine Channels gesetzt sind, sind ALLE erlaubt!
     */
    public boolean isChannelAllowed(String guildId, String channelId) {
        List<String> allowed = getAllowedChannels(guildId);

        // Wenn keine Channels gesetzt sind, sind alle erlaubt
        if (allowed.isEmpty()) {
            return true;
        }

        return allowed.contains(channelId);
    }

    /**
     * Löscht alle erlaubten Channels (Reset)
     */
    public void clearAllowedChannels(String guildId) {
        try {
            String sql = "DELETE FROM allowed_channels WHERE guild_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, guildId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== NSFW SETTINGS ====================

    /**
     * Aktiviert NSFW Mode für einen Server
     */
    public void enableNSFW(String guildId) {
        try {
            String sql = "INSERT INTO server_settings (guild_id, nsfw_enabled) VALUES (?, 1) " +
                    "ON CONFLICT(guild_id) DO UPDATE SET nsfw_enabled = 1";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, guildId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deaktiviert NSFW Mode für einen Server
     */
    public void disableNSFW(String guildId) {
        try {
            String sql = "INSERT INTO server_settings (guild_id, nsfw_enabled) VALUES (?, 0) " +
                    "ON CONFLICT(guild_id) DO UPDATE SET nsfw_enabled = 0";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, guildId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prüft ob NSFW für Server aktiviert ist
     */
    public boolean isNSFWEnabled(String guildId) {
        try {
            String sql = "SELECT nsfw_enabled FROM server_settings WHERE guild_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                boolean enabled = rs.getInt("nsfw_enabled") == 1;
                rs.close();
                pstmt.close();
                return enabled;
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Default: NSFW deaktiviert
    }

    // ==================== SETUP STATUS ====================

    /**
     * Markiert Setup als abgeschlossen
     */
    public void setSetupComplete(String guildId) {
        try {
            String sql = "INSERT INTO server_settings (guild_id, setup_complete) VALUES (?, 1) " +
                    "ON CONFLICT(guild_id) DO UPDATE SET setup_complete = 1";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, guildId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prüft ob Setup abgeschlossen ist
     */
    public boolean isSetupComplete(String guildId) {
        try {
            String sql = "SELECT setup_complete FROM server_settings WHERE guild_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                boolean complete = rs.getInt("setup_complete") == 1;
                rs.close();
                pstmt.close();
                return complete;
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Löscht alle Einstellungen für einen Server
     */
    public void resetServer(String guildId) {
        try {
            // Lösche Settings
            String sql1 = "DELETE FROM server_settings WHERE guild_id = ?";
            PreparedStatement pstmt1 = connection.prepareStatement(sql1);
            pstmt1.setString(1, guildId);
            pstmt1.executeUpdate();
            pstmt1.close();

            // Lösche Channels
            clearAllowedChannels(guildId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}