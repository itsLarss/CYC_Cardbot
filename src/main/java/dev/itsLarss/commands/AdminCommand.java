package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin Command - NUR für Bot Owner!
 * Funktionen:
 * - User-Daten löschen (DSGVO-konform)
 * - User-Statistiken anzeigen
 * - User resetten
 */
public class AdminCommand extends ListenerAdapter {

    // WICHTIG: Hier deine Discord User-ID eintragen!
    private static final String BOT_OWNER_ID = "768110753106231316"; // ← ERSETZEN!

    // Speichert Lösch-Anfragen: MessageID -> DeleteRequest
    private static Map<String, DeleteRequest> activeRequests = new HashMap<>();

    public CommandData getCommandData() {
        return Commands.slash("admin", "🔧 Admin-Befehle (nur Bot Owner)")
                .addSubcommands(
                        new SubcommandData("userdata", "Zeige alle Daten eines Users")
                                .addOption(OptionType.USER, "user", "User auswählen", true),
                        new SubcommandData("deletecoins", "Lösche Coins eines Users")
                                .addOption(OptionType.USER, "user", "User auswählen", true),
                        new SubcommandData("deletecards", "Lösche alle Karten eines Users")
                                .addOption(OptionType.USER, "user", "User auswählen", true),
                        new SubcommandData("deleteall", "Lösche ALLE Daten eines Users (DSGVO)")
                                .addOption(OptionType.USER, "user", "User auswählen", true),
                        new SubcommandData("stats", "Zeige Bot-Statistiken")
                );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("admin")) return;

        // NUR Bot Owner darf Admin-Commands nutzen!
        if (!event.getUser().getId().equals(BOT_OWNER_ID)) {
            event.reply("❌ Dieser Command ist nur für den Bot Owner!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String subcommand = event.getSubcommandName();

        switch (subcommand) {
            case "userdata":
                handleUserData(event);
                break;
            case "deletecoins":
                handleDeleteCoins(event);
                break;
            case "deletecards":
                handleDeleteCards(event);
                break;
            case "deleteall":
                handleDeleteAll(event);
                break;
            case "stats":
                handleStats(event);
                break;
        }
    }

    /**
     * Zeigt alle Daten eines Users
     */
    private void handleUserData(SlashCommandInteractionEvent event) {
        User targetUser = event.getOption("user").getAsUser();
        String userId = targetUser.getId();

        DatabaseManager db = CardBot.getDatabase();

        // Sammle Daten
        int coins = db.getUserCoins(userId);
        int totalCards = db.getTotalCardCount(userId);
        int uniqueCards = db.getUniqueCardCount(userId);

        // Hole zusätzliche Daten
        String lastDaily = getLastDaily(userId);
        int totalTrades = getTotalTrades(userId);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔍 User-Daten: " + targetUser.getName())
                .setThumbnail(targetUser.getAvatarUrl())
                .setColor(0x3498DB);

        embed.addField("👤 User Info",
                "**ID:** " + userId + "\n" +
                        "**Name:** " + targetUser.getName() + "\n" +
                        "**Tag:** " + targetUser.getAsTag(),
                false);

        embed.addField("💰 Coins",
                "**" + coins + "** 🪙",
                true);

        embed.addField("🎴 Karten",
                "**Total:** " + totalCards + "\n" +
                        "**Verschieden:** " + uniqueCards,
                true);

        embed.addField("📊 Aktivität",
                "**Letzter Daily:** " + lastDaily + "\n" +
                        "**Trades:** " + totalTrades,
                true);

        embed.setFooter("Nutze /admin delete... um Daten zu löschen");

        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Lösche nur Coins eines Users
     */
    private void handleDeleteCoins(SlashCommandInteractionEvent event) {
        User targetUser = event.getOption("user").getAsUser();
        String userId = targetUser.getId();

        DatabaseManager db = CardBot.getDatabase();
        int currentCoins = db.getUserCoins(userId);

        if (currentCoins == 0) {
            event.reply("ℹ️ User hat bereits 0 Coins!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Bestätigungs-Embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⚠️ Coins löschen?")
                .setDescription(
                        "**User:** " + targetUser.getAsMention() + "\n" +
                                "**Aktuelle Coins:** " + currentCoins + " 🪙\n\n" +
                                "Nach dem Löschen: **0 Coins**"
                )
                .setColor(0xE74C3C);

        Button confirmButton = Button.danger("admin_delete_coins_confirm", "✅ Ja, Coins löschen");
        Button cancelButton = Button.secondary("admin_delete_cancel", "❌ Abbrechen");

        event.replyEmbeds(embed.build())
                .addActionRow(confirmButton, cancelButton)
                .queue(message -> {
                    message.retrieveOriginal().queue(msg -> {
                        activeRequests.put(msg.getId(), new DeleteRequest(userId, "coins", targetUser.getName()));

                        // Timeout nach 30 Sekunden
                        new Thread(() -> {
                            try {
                                Thread.sleep(30000);
                                activeRequests.remove(msg.getId());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                });
    }

    /**
     * Lösche alle Karten eines Users
     */
    private void handleDeleteCards(SlashCommandInteractionEvent event) {
        User targetUser = event.getOption("user").getAsUser();
        String userId = targetUser.getId();

        DatabaseManager db = CardBot.getDatabase();
        int totalCards = db.getTotalCardCount(userId);
        int uniqueCards = db.getUniqueCardCount(userId);

        if (totalCards == 0) {
            event.reply("ℹ️ User hat keine Karten!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Bestätigungs-Embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⚠️ Karten löschen?")
                .setDescription(
                        "**User:** " + targetUser.getAsMention() + "\n" +
                                "**Karten gesamt:** " + totalCards + "\n" +
                                "**Verschiedene Karten:** " + uniqueCards + "\n\n" +
                                "⚠️ Diese Aktion löscht **ALLE** Karten!"
                )
                .setColor(0xE74C3C);

        Button confirmButton = Button.danger("admin_delete_cards_confirm", "✅ Ja, alle Karten löschen");
        Button cancelButton = Button.secondary("admin_delete_cancel", "❌ Abbrechen");

        event.replyEmbeds(embed.build())
                .addActionRow(confirmButton, cancelButton)
                .queue(message -> {
                    message.retrieveOriginal().queue(msg -> {
                        activeRequests.put(msg.getId(), new DeleteRequest(userId, "cards", targetUser.getName()));

                        new Thread(() -> {
                            try {
                                Thread.sleep(30000);
                                activeRequests.remove(msg.getId());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                });
    }

    /**
     * Lösche ALLE Daten eines Users (DSGVO-konform)
     */
    private void handleDeleteAll(SlashCommandInteractionEvent event) {
        User targetUser = event.getOption("user").getAsUser();
        String userId = targetUser.getId();

        DatabaseManager db = CardBot.getDatabase();
        int coins = db.getUserCoins(userId);
        int totalCards = db.getTotalCardCount(userId);

        // Bestätigungs-Embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🚨 ALLE DATEN LÖSCHEN? (DSGVO)")
                .setDescription(
                        "**User:** " + targetUser.getAsMention() + "\n\n" +
                                "**Wird gelöscht:**\n" +
                                "• Alle Karten (" + totalCards + "x)\n" +
                                "• Alle Coins (" + coins + " 🪙)\n" +
                                "• Daily Claim Historie\n" +
                                "• Trade Historie\n\n" +
                                "⚠️ **WARNUNG:** Diese Aktion kann NICHT rückgängig gemacht werden!"
                )
                .setColor(0xFF0000);

        Button confirmButton = Button.danger("admin_delete_all_confirm", "🗑️ JA, ALLE DATEN LÖSCHEN");
        Button cancelButton = Button.secondary("admin_delete_cancel", "❌ Abbrechen");

        event.replyEmbeds(embed.build())
                .addActionRow(confirmButton, cancelButton)
                .queue(message -> {
                    message.retrieveOriginal().queue(msg -> {
                        activeRequests.put(msg.getId(), new DeleteRequest(userId, "all", targetUser.getName()));

                        new Thread(() -> {
                            try {
                                Thread.sleep(30000);
                                activeRequests.remove(msg.getId());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                });
    }

    /**
     * Zeige Bot-Statistiken
     */
    private void handleStats(SlashCommandInteractionEvent event) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:cards.db");

            // Zähle unique Users
            PreparedStatement pstmt1 = conn.prepareStatement(
                    "SELECT COUNT(DISTINCT user_id) as count FROM user_cards"
            );
            ResultSet rs1 = pstmt1.executeQuery();
            int totalUsers = rs1.next() ? rs1.getInt("count") : 0;
            rs1.close();
            pstmt1.close();

            // Zähle total Karten
            PreparedStatement pstmt2 = conn.prepareStatement(
                    "SELECT SUM(quantity) as total FROM user_cards"
            );
            ResultSet rs2 = pstmt2.executeQuery();
            int totalCards = rs2.next() ? rs2.getInt("total") : 0;
            rs2.close();
            pstmt2.close();

            // Zähle total Coins
            PreparedStatement pstmt3 = conn.prepareStatement(
                    "SELECT SUM(coins) as total FROM user_coins"
            );
            ResultSet rs3 = pstmt3.executeQuery();
            int totalCoins = rs3.next() ? rs3.getInt("total") : 0;
            rs3.close();
            pstmt3.close();

            // Zähle Trades
            PreparedStatement pstmt4 = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM trade_history"
            );
            ResultSet rs4 = pstmt4.executeQuery();
            int totalTrades = rs4.next() ? rs4.getInt("count") : 0;
            rs4.close();
            pstmt4.close();

            conn.close();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("📊 Bot-Statistiken")
                    .setColor(0x9B59B6);

            embed.addField("👥 Users", "**" + totalUsers + "** aktive Sammler", true);
            embed.addField("🎴 Karten", "**" + totalCards + "** gesammelt", true);
            embed.addField("💰 Coins", "**" + totalCoins + "** 🪙 im Umlauf", true);
            embed.addField("🔄 Trades", "**" + totalTrades + "** Trades", true);

            event.replyEmbeds(embed.build()).queue();

        } catch (Exception e) {
            e.printStackTrace();
            event.reply("❌ Fehler beim Laden der Statistiken!")
                    .setEphemeral(true)
                    .queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();

        if (!buttonId.startsWith("admin_delete_")) return;

        // Nur Bot Owner
        if (!event.getUser().getId().equals(BOT_OWNER_ID)) {
            event.reply("❌ Nicht autorisiert!").setEphemeral(true).queue();
            return;
        }

        String messageId = event.getMessageId();
        DeleteRequest request = activeRequests.get(messageId);

        if (request == null) {
            event.reply("❌ Diese Anfrage ist abgelaufen!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (buttonId.equals("admin_delete_cancel")) {
            activeRequests.remove(messageId);
            event.editMessage("❌ Abgebrochen - Keine Daten wurden gelöscht.")
                    .setEmbeds()
                    .setComponents()
                    .queue();
            return;
        }

        // Führe Löschung aus
        DatabaseManager db = CardBot.getDatabase();
        EmbedBuilder resultEmbed = new EmbedBuilder();

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:cards.db");

            switch (request.type) {
                case "coins":
                    db.setUserCoins(request.userId, 0);
                    resultEmbed.setTitle("✅ Coins gelöscht!")
                            .setDescription("Coins von **" + request.userName + "** wurden auf 0 gesetzt.")
                            .setColor(0x2ECC71);
                    break;

                case "cards":
                    PreparedStatement pstmt1 = conn.prepareStatement(
                            "DELETE FROM user_cards WHERE user_id = ?"
                    );
                    pstmt1.setString(1, request.userId);
                    int deletedCards = pstmt1.executeUpdate();
                    pstmt1.close();

                    resultEmbed.setTitle("✅ Karten gelöscht!")
                            .setDescription("Alle Karten von **" + request.userName + "** wurden gelöscht.\n" +
                                    "(**" + deletedCards + "** Einträge entfernt)")
                            .setColor(0x2ECC71);
                    break;

                case "all":
                    // Lösche ALLES
                    PreparedStatement[] statements = new PreparedStatement[4];

                    statements[0] = conn.prepareStatement("DELETE FROM user_cards WHERE user_id = ?");
                    statements[1] = conn.prepareStatement("DELETE FROM user_coins WHERE user_id = ?");
                    statements[2] = conn.prepareStatement("DELETE FROM daily_claims WHERE user_id = ?");
                    statements[3] = conn.prepareStatement("DELETE FROM trade_history WHERE user1_id = ? OR user2_id = ?");

                    int totalDeleted = 0;
                    for (int i = 0; i < 3; i++) {
                        statements[i].setString(1, request.userId);
                        totalDeleted += statements[i].executeUpdate();
                        statements[i].close();
                    }

                    statements[3].setString(1, request.userId);
                    statements[3].setString(2, request.userId);
                    totalDeleted += statements[3].executeUpdate();
                    statements[3].close();

                    resultEmbed.setTitle("🗑️ ALLE DATEN GELÖSCHT (DSGVO)")
                            .setDescription(
                                    "Alle Daten von **" + request.userName + "** wurden gelöscht:\n\n" +
                                            "✅ Karten\n" +
                                            "✅ Coins\n" +
                                            "✅ Daily Claims\n" +
                                            "✅ Trade Historie\n\n" +
                                            "**" + totalDeleted + "** Einträge entfernt."
                            )
                            .setColor(0x2ECC71);
                    break;
            }

            conn.close();
            activeRequests.remove(messageId);

            event.editMessageEmbeds(resultEmbed.build())
                    .setComponents()
                    .queue();

        } catch (Exception e) {
            e.printStackTrace();
            event.reply("❌ Fehler beim Löschen der Daten!")
                    .setEphemeral(true)
                    .queue();
        }
    }

    // ==================== HELPER METHODS ====================

    private String getLastDaily(String userId) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:cards.db");
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT last_claim FROM daily_claims WHERE user_id = ?"
            );
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            String result = "Nie";
            if (rs.next()) {
                result = rs.getString("last_claim");
            }

            rs.close();
            pstmt.close();
            conn.close();

            return result;
        } catch (Exception e) {
            return "Unbekannt";
        }
    }

    private int getTotalTrades(String userId) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:cards.db");
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM trade_history WHERE user1_id = ? OR user2_id = ?"
            );
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            if (rs.next()) {
                count = rs.getInt("count");
            }

            rs.close();
            pstmt.close();
            conn.close();

            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== HELPER CLASS ====================

    private static class DeleteRequest {
        String userId;
        String type; // "coins", "cards", "all"
        String userName;

        DeleteRequest(String userId, String type, String userName) {
            this.userId = userId;
            this.type = type;
            this.userName = userName;
        }
    }
}