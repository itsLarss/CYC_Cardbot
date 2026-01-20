package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Wishlist Command - Verwalte deine Wunschliste
 */
public class WishlistCommand extends ListenerAdapter {

    public CommandData getCommandData() {
        return Commands.slash("wishlist", "Verwalte deine Wunschliste")
                .addSubcommands(
                        new SubcommandData("add", "Füge eine Karte zur Wunschliste hinzu")
                                .addOption(OptionType.STRING, "karte", "Name der Karte", true),
                        new SubcommandData("remove", "Entferne eine Karte von der Wunschliste")
                                .addOption(OptionType.STRING, "karte", "Name der Karte", true),
                        new SubcommandData("show", "Zeige deine Wunschliste")
                                .addOption(OptionType.USER, "user", "User dessen Wunschliste du sehen möchtest", false)
                );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("wishlist")) return;

        String subcommand = event.getSubcommandName();

        if (subcommand == null) return;

        switch (subcommand) {
            case "add":
                handleAdd(event);
                break;
            case "remove":
                handleRemove(event);
                break;
            case "show":
                handleShow(event);
                break;
        }
    }

    private void handleAdd(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        String cardName = event.getOption("karte").getAsString();

        Card card = CardRegistry.getCardByName(cardName);

        if (card == null) {
            event.reply("❌ Karte '" + cardName + "' nicht gefunden!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:cards.db");

            // Erstelle Tabelle falls nicht vorhanden
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS wishlist (" +
                            "user_id TEXT, " +
                            "card_id INTEGER, " +
                            "added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "PRIMARY KEY (user_id, card_id))"
            );

            // Füge zur Wishlist hinzu
            String sql = "INSERT OR IGNORE INTO wishlist (user_id, card_id) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setInt(2, card.getId());

            int rows = pstmt.executeUpdate();

            pstmt.close();
            conn.close();

            if (rows > 0) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("⭐ Zur Wunschliste hinzugefügt!")
                        .setDescription(card.getRarity().getEmoji() + " **" + card.getName() + "**")
                        .setColor(0xF1C40F);

                event.replyEmbeds(embed.build()).queue();
            } else {
                event.reply("ℹ️ " + card.getName() + " ist bereits auf deiner Wunschliste!")
                        .setEphemeral(true)
                        .queue();
            }

        } catch (Exception e) {
            e.printStackTrace();
            event.reply("❌ Fehler beim Hinzufügen!")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handleRemove(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        String cardName = event.getOption("karte").getAsString();

        Card card = CardRegistry.getCardByName(cardName);

        if (card == null) {
            event.reply("❌ Karte '" + cardName + "' nicht gefunden!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:cards.db");

            String sql = "DELETE FROM wishlist WHERE user_id = ? AND card_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setInt(2, card.getId());

            int rows = pstmt.executeUpdate();

            pstmt.close();
            conn.close();

            if (rows > 0) {
                event.reply("✅ " + card.getName() + " wurde von deiner Wunschliste entfernt!")
                        .queue();
            } else {
                event.reply("ℹ️ " + card.getName() + " war nicht auf deiner Wunschliste!")
                        .setEphemeral(true)
                        .queue();
            }

        } catch (Exception e) {
            e.printStackTrace();
            event.reply("❌ Fehler beim Entfernen!")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handleShow(SlashCommandInteractionEvent event) {
        String targetUserId = event.getOption("user") != null
                ? event.getOption("user").getAsUser().getId()
                : event.getUser().getId();

        String targetUserName = event.getOption("user") != null
                ? event.getOption("user").getAsUser().getName()
                : event.getUser().getName();

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:cards.db");

            String sql = "SELECT card_id FROM wishlist WHERE user_id = ? ORDER BY added_at";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, targetUserId);

            ResultSet rs = pstmt.executeQuery();

            List<Card> wishlistCards = new ArrayList<>();
            while (rs.next()) {
                Card card = CardRegistry.getCard(rs.getInt("card_id"));
                if (card != null) {
                    wishlistCards.add(card);
                }
            }

            rs.close();
            pstmt.close();
            conn.close();

            if (wishlistCards.isEmpty()) {
                event.reply("📭 " + targetUserName + " hat keine Karten auf der Wunschliste!")
                        .queue();
                return;
            }

            // Erstelle Embed
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("⭐ " + targetUserName + "'s Wunschliste")
                    .setDescription("**" + wishlistCards.size() + "** Karten auf der Wunschliste")
                    .setColor(0xF1C40F);

            // Zeige Karten
            int displayLimit = Math.min(wishlistCards.size(), 15);
            for (int i = 0; i < displayLimit; i++) {
                Card card = wishlistCards.get(i);
                embed.addField(
                        card.getRarity().getEmoji() + " " + card.getName(),
                        "*" + card.getRarity().getName() + "* - " + card.getSeries(),
                        false
                );
            }

            if (wishlistCards.size() > displayLimit) {
                embed.setFooter("... und " + (wishlistCards.size() - displayLimit) + " weitere");
            }

            event.replyEmbeds(embed.build()).queue();

        } catch (Exception e) {
            e.printStackTrace();
            event.reply("❌ Fehler beim Laden der Wunschliste!")
                    .setEphemeral(true)
                    .queue();
        }
    }
}