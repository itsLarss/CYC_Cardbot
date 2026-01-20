package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Leaderboard Command - Zeigt die Top-Sammler
 */
public class LeaderboardCommand extends ListenerAdapter {

    public CommandData getCommandData() {
        return Commands.slash("leaderboard", "Zeige die Top-Sammler")
                .addOption(OptionType.STRING, "kategorie", "Nach was sortieren? (karten/coins)", false);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("leaderboard")) return;

        String category = event.getOption("kategorie") != null
                ? event.getOption("kategorie").getAsString().toLowerCase()
                : "karten";

        List<LeaderboardEntry> entries = new ArrayList<>();

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:cards.db");
            PreparedStatement pstmt;

            if (category.equals("coins")) {
                // Top Coin-Besitzer
                String sql = "SELECT user_id, coins FROM user_coins ORDER BY coins DESC LIMIT 10";
                pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    entries.add(new LeaderboardEntry(
                            rs.getString("user_id"),
                            rs.getInt("coins")
                    ));
                }

                rs.close();

            } else {
                // Top Kartensammler
                String sql = "SELECT user_id, SUM(quantity) as total " +
                        "FROM user_cards " +
                        "GROUP BY user_id " +
                        "ORDER BY total DESC " +
                        "LIMIT 10";

                pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    entries.add(new LeaderboardEntry(
                            rs.getString("user_id"),
                            rs.getInt("total")
                    ));
                }

                rs.close();
            }

            pstmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            event.reply("❌ Fehler beim Laden des Leaderboards!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (entries.isEmpty()) {
            event.reply("📭 Noch keine Daten vorhanden!")
                    .queue();
            return;
        }

        // Erstelle Embed
        String title = category.equals("coins")
                ? "💰 Top Coin-Besitzer"
                : "🎴 Top Sammler";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setColor(0xFFD700);

        String[] medals = {"🥇", "🥈", "🥉"};

        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);

            // Hole User-Namen
            String userName;
            try {
                User user = CardBot.getJDA().retrieveUserById(entry.userId).complete();
                userName = user.getName();
            } catch (Exception e) {
                userName = "User " + entry.userId;
            }

            String medal = i < 3 ? medals[i] : "**" + (i + 1) + ".**";

            String valueText = category.equals("coins")
                    ? entry.value + " 🪙 Coins"
                    : entry.value + " Karten";

            embed.addField(
                    medal + " " + userName,
                    valueText,
                    false
            );
        }

        event.replyEmbeds(embed.build()).queue();
    }

    // Helper-Klasse
    private static class LeaderboardEntry {
        String userId;
        int value;

        LeaderboardEntry(String userId, int value) {
            this.userId = userId;
            this.value = value;
        }
    }
}