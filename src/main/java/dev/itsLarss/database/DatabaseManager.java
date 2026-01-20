package dev.itsLarss.database;

import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRegistry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:cards.db";
    private Connection connection;

    /**
     * Initialisiert die Datenbank und erstellt Tabellen
     */
    public void initialize() {
        try {
            // Lade SQLite JDBC Treiber
            Class.forName("org.sqlite.JDBC");

            // Verbinde zur Datenbank
            connection = DriverManager.getConnection(DB_URL);

            createTables();

            System.out.println("✅ Datenbank erfolgreich initialisiert!");

        } catch (Exception e) {
            System.err.println("❌ Fehler beim Initialisieren der Datenbank:");
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Tabelle für User-Karten
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS user_cards (" +
                        "user_id TEXT NOT NULL, " +
                        "card_id INTEGER NOT NULL, " +
                        "quantity INTEGER DEFAULT 1, " +
                        "obtained_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (user_id, card_id))"
        );

        // Tabelle für Daily Claims
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS daily_claims (" +
                        "user_id TEXT PRIMARY KEY, " +
                        "last_claim TIMESTAMP)"
        );

        // Tabelle für Coins
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS user_coins (" +
                        "user_id TEXT PRIMARY KEY, " +
                        "coins INTEGER DEFAULT 0)"
        );

        // Tabelle für Trade History
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS trade_history (" +
                        "trade_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user1_id TEXT, " +
                        "user2_id TEXT, " +
                        "user1_card_id INTEGER, " +
                        "user2_card_id INTEGER, " +
                        "traded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        );

        stmt.close();
    }

    // ==================== KARTEN-VERWALTUNG ====================

    /**
     * Fügt eine Karte zur Sammlung eines Users hinzu
     */
    public void addCardToUser(String userId, int cardId) {
        try {
            String sql = "INSERT INTO user_cards (user_id, card_id, quantity) " +
                    "VALUES (?, ?, 1) " +
                    "ON CONFLICT(user_id, card_id) DO UPDATE SET quantity = quantity + 1";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setInt(2, cardId);
            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Entfernt eine Karte von einem User
     * FIXIERT: Löscht die Zeile wenn quantity 0 erreicht!
     */
    public boolean removeCardFromUser(String userId, int cardId) {
        try {
            // Prüfe erst ob User die Karte hat
            int currentQuantity = getUserCardQuantity(userId, cardId);
            if (currentQuantity < 1) {
                return false;
            }

            // Wenn nur 1 Karte vorhanden: LÖSCHE die Zeile komplett
            if (currentQuantity == 1) {
                String sql = "DELETE FROM user_cards WHERE user_id = ? AND card_id = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, userId);
                pstmt.setInt(2, cardId);
                pstmt.executeUpdate();
                pstmt.close();
            }
            // Wenn mehrere Karten: Reduziere quantity
            else {
                String sql = "UPDATE user_cards SET quantity = quantity - 1 " +
                        "WHERE user_id = ? AND card_id = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, userId);
                pstmt.setInt(2, cardId);
                pstmt.executeUpdate();
                pstmt.close();
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gibt alle Karten eines Users zurück
     */
    public Map<Card, Integer> getUserCards(String userId) {
        Map<Card, Integer> userCards = new LinkedHashMap<>();

        try {
            String sql = "SELECT card_id, quantity FROM user_cards " +
                    "WHERE user_id = ? AND quantity > 0 ORDER BY card_id";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int cardId = rs.getInt("card_id");
                int quantity = rs.getInt("quantity");
                Card card = CardRegistry.getCard(cardId);

                if (card != null) {
                    userCards.put(card, quantity);
                }
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userCards;
    }

    /**
     * Gibt die Anzahl einer bestimmten Karte zurück, die ein User besitzt
     */
    public int getUserCardQuantity(String userId, int cardId) {
        try {
            String sql = "SELECT quantity FROM user_cards WHERE user_id = ? AND card_id = ?";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setInt(2, cardId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int quantity = rs.getInt("quantity");
                rs.close();
                pstmt.close();
                return quantity;
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Bereinigt alle Einträge mit quantity = 0 (Cleanup-Funktion)
     */
    public void cleanupZeroQuantityCards() {
        try {
            String sql = "DELETE FROM user_cards WHERE quantity <= 0";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            int deleted = pstmt.executeUpdate();
            pstmt.close();

            if (deleted > 0) {
                System.out.println("✅ Cleanup: " + deleted + " leere Karten-Einträge entfernt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== COINS-VERWALTUNG ====================

    /**
     * Gibt die Coins eines Users zurück
     */
    public int getUserCoins(String userId) {
        try {
            String sql = "SELECT coins FROM user_coins WHERE user_id = ?";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int coins = rs.getInt("coins");
                rs.close();
                pstmt.close();
                return coins;
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Fügt Coins zu einem User hinzu (oder zieht ab wenn negativ)
     */
    public void addCoins(String userId, int amount) {
        try {
            String sql = "INSERT INTO user_coins (user_id, coins) VALUES (?, ?) " +
                    "ON CONFLICT(user_id) DO UPDATE SET coins = coins + ?";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setInt(2, amount);
            pstmt.setInt(3, amount);
            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setzt die Coins eines Users auf einen bestimmten Wert
     */
    public void setUserCoins(String userId, int coins) {
        try {
            String sql = "INSERT INTO user_coins (user_id, coins) VALUES (?, ?) " +
                    "ON CONFLICT(user_id) DO UPDATE SET coins = ?";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setInt(2, coins);
            pstmt.setInt(3, coins);
            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== DAILY CLAIMS ====================

    /**
     * Prüft ob ein User daily claimen kann
     */
    public boolean canClaimDaily(String userId) {
        try {
            String sql = "SELECT last_claim FROM daily_claims WHERE user_id = ?";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Timestamp lastClaim = rs.getTimestamp("last_claim");
                LocalDateTime lastClaimTime = lastClaim.toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();

                rs.close();
                pstmt.close();

                // Prüfe ob 24 Stunden vergangen sind
                return lastClaimTime.plusDays(1).isBefore(now);
            }

            rs.close();
            pstmt.close();

            // Noch nie geclaimed
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gibt die verbleibende Zeit bis zum nächsten Daily zurück (in Sekunden)
     */
    public long getTimeUntilNextDaily(String userId) {
        try {
            String sql = "SELECT last_claim FROM daily_claims WHERE user_id = ?";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Timestamp lastClaim = rs.getTimestamp("last_claim");
                LocalDateTime lastClaimTime = lastClaim.toLocalDateTime();
                LocalDateTime nextClaim = lastClaimTime.plusDays(1);
                LocalDateTime now = LocalDateTime.now();

                rs.close();
                pstmt.close();

                return java.time.Duration.between(now, nextClaim).getSeconds();
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Setzt den Daily Claim für einen User
     */
    public void setDailyClaim(String userId) {
        try {
            String sql = "INSERT INTO user_coins (user_id, coins) VALUES (?, 0) " +
                    "ON CONFLICT(user_id) DO NOTHING";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
            pstmt.close();

            sql = "INSERT INTO daily_claims (user_id, last_claim) VALUES (?, CURRENT_TIMESTAMP) " +
                    "ON CONFLICT(user_id) DO UPDATE SET last_claim = CURRENT_TIMESTAMP";

            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== STATISTIKEN ====================

    /**
     * Gibt die Gesamtanzahl der Karten eines Users zurück
     */
    public int getTotalCardCount(String userId) {
        try {
            String sql = "SELECT SUM(quantity) as total FROM user_cards WHERE user_id = ? AND quantity > 0";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                rs.close();
                pstmt.close();
                return total;
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Gibt die Anzahl verschiedener Karten zurück, die ein User besitzt
     */
    public int getUniqueCardCount(String userId) {
        try {
            String sql = "SELECT COUNT(*) as count FROM user_cards WHERE user_id = ? AND quantity > 0";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                rs.close();
                pstmt.close();
                return count;
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Gibt die Connection zurück (für ServerSettingsManager)
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Schließt die Datenbankverbindung
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}