package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Trade Command - Ermöglicht das Tauschen von Karten zwischen Spielern
 *
 * WICHTIG: Diese Klasse muss in CardBot.java registriert werden!
 *
 * In CardBot.java hinzufügen:
 * - .addCommands(new TradeCommand().getCommandData())
 * - .addEventListener(new TradeCommand())
 */
public class TradeCommand extends ListenerAdapter {

    // Speichert aktive Trades: MessageID -> Trade Info
    private static Map<String, TradeOffer> activeTrades = new HashMap<>();

    public CommandData getCommandData() {
        return Commands.slash("trade", "Tausche Karten mit einem anderen Spieler")
                .addOption(OptionType.USER, "user", "Mit wem möchtest du tauschen?", true)
                .addOption(OptionType.STRING, "give", "Karte die du gibst (Name)", true)
                .addOption(OptionType.STRING, "want", "Karte die du haben möchtest (Name)", true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("trade")) return;

        User initiator = event.getUser();
        User partner = event.getOption("user").getAsUser();
        String giveCardName = event.getOption("give").getAsString();
        String wantCardName = event.getOption("want").getAsString();

        // Validierung: Nicht mit sich selbst handeln
        if (partner.getId().equals(initiator.getId())) {
            event.reply("❌ Du kannst nicht mit dir selbst handeln!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Validierung: Nicht mit Bots handeln
        if (partner.isBot()) {
            event.reply("❌ Du kannst nicht mit Bots handeln!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Finde Karten
        Card giveCard = CardRegistry.getCardByName(giveCardName);
        Card wantCard = CardRegistry.getCardByName(wantCardName);

        if (giveCard == null) {
            event.reply("❌ Karte '" + giveCardName + "' nicht gefunden!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (wantCard == null) {
            event.reply("❌ Karte '" + wantCardName + "' nicht gefunden!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        DatabaseManager db = CardBot.getDatabase();

        // Prüfe ob Initiator die Karte besitzt
        if (db.getUserCardQuantity(initiator.getId(), giveCard.getId()) < 1) {
            event.reply("❌ Du besitzt keine '" + giveCard.getName() + "'!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Prüfe ob Partner die Karte besitzt
        if (db.getUserCardQuantity(partner.getId(), wantCard.getId()) < 1) {
            event.reply("❌ " + partner.getName() + " besitzt keine '" + wantCard.getName() + "'!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Erstelle Trade Embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔄 Trade-Angebot")
                .setDescription(initiator.getAsMention() + " möchte mit " + partner.getAsMention() + " tauschen!")
                .setColor(0xF39C12);

        embed.addField(
                initiator.getName() + " gibt:",
                giveCard.getRarity().getEmoji() + " **" + giveCard.getName() + "** (" + giveCard.getRarity().getName() + ")\n" +
                        "*" + giveCard.getDescription() + "*",
                true
        );

        embed.addField(
                partner.getName() + " gibt:",
                wantCard.getRarity().getEmoji() + " **" + wantCard.getName() + "** (" + wantCard.getRarity().getName() + ")\n" +
                        "*" + wantCard.getDescription() + "*",
                true
        );

        embed.setFooter(partner.getName() + " hat 60 Sekunden zum Akzeptieren");

        // Erstelle Buttons
        Button acceptButton = Button.success("trade_accept", "✅ Akzeptieren");
        Button declineButton = Button.danger("trade_decline", "❌ Ablehnen");

        // Sende Nachricht
        event.replyEmbeds(embed.build())
                .addActionRow(acceptButton, declineButton)
                .queue(message -> {
                    // Speichere Trade Info
                    message.retrieveOriginal().queue(msg -> {
                        TradeOffer offer = new TradeOffer(
                                initiator.getId(),
                                partner.getId(),
                                giveCard.getId(),
                                wantCard.getId()
                        );
                        activeTrades.put(msg.getId(), offer);

                        // Entferne Trade nach 60 Sekunden
                        CardBot.getJDA().retrieveUserById(initiator.getId()).queue(user -> {
                            try {
                                Thread.sleep(60000);
                                if (activeTrades.containsKey(msg.getId())) {
                                    activeTrades.remove(msg.getId());

                                    EmbedBuilder timeoutEmbed = new EmbedBuilder()
                                            .setTitle("⏱️ Trade abgelaufen")
                                            .setDescription("Der Trade wurde nicht rechtzeitig akzeptiert.")
                                            .setColor(0x95A5A6);

                                    msg.editMessageEmbeds(timeoutEmbed.build())
                                            .setComponents()
                                            .queue();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                    });
                });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();

        if (!buttonId.startsWith("trade_")) return;

        String messageId = event.getMessageId();
        TradeOffer offer = activeTrades.get(messageId);

        if (offer == null) {
            event.reply("❌ Dieser Trade ist nicht mehr verfügbar!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Nur der Partner kann akzeptieren/ablehnen
        if (!event.getUser().getId().equals(offer.partnerId)) {
            event.reply("❌ Nur " + event.getJDA().retrieveUserById(offer.partnerId).complete().getName() +
                            " kann auf diesen Trade reagieren!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (buttonId.equals("trade_accept")) {
            // Trade akzeptiert - führe aus
            executeTrade(event, offer);
        } else if (buttonId.equals("trade_decline")) {
            // Trade abgelehnt
            activeTrades.remove(messageId);

            User partner = event.getJDA().retrieveUserById(offer.partnerId).complete();

            EmbedBuilder declineEmbed = new EmbedBuilder()
                    .setTitle("❌ Trade abgelehnt")
                    .setDescription(partner.getAsMention() + " hat den Trade abgelehnt.")
                    .setColor(0xE74C3C);

            event.editMessageEmbeds(declineEmbed.build())
                    .setComponents()
                    .queue();
        }
    }

    private void executeTrade(ButtonInteractionEvent event, TradeOffer offer) {
        DatabaseManager db = CardBot.getDatabase();

        // Nochmal prüfen ob beide Karten noch vorhanden sind
        if (db.getUserCardQuantity(offer.initiatorId, offer.giveCardId) < 1) {
            event.reply("❌ Der Initiator besitzt die Karte nicht mehr!")
                    .setEphemeral(true)
                    .queue();
            activeTrades.remove(event.getMessageId());
            return;
        }

        if (db.getUserCardQuantity(offer.partnerId, offer.wantCardId) < 1) {
            event.reply("❌ Du besitzt die Karte nicht mehr!")
                    .setEphemeral(true)
                    .queue();
            activeTrades.remove(event.getMessageId());
            return;
        }

        try {
            // Führe Trade aus
            // 1. Entferne Karten
            db.removeCardFromUser(offer.initiatorId, offer.giveCardId);
            db.removeCardFromUser(offer.partnerId, offer.wantCardId);

            // 2. Füge Karten hinzu
            db.addCardToUser(offer.partnerId, offer.giveCardId);
            db.addCardToUser(offer.initiatorId, offer.wantCardId);

            // 3. Speichere in Trade History
            saveTradeHistory(offer);

            // Entferne aus aktiven Trades
            activeTrades.remove(event.getMessageId());

            // Hole Karten-Informationen
            Card giveCard = CardRegistry.getCard(offer.giveCardId);
            Card wantCard = CardRegistry.getCard(offer.wantCardId);

            User initiator = event.getJDA().retrieveUserById(offer.initiatorId).complete();
            User partner = event.getJDA().retrieveUserById(offer.partnerId).complete();

            // Erfolgs-Embed
            EmbedBuilder successEmbed = new EmbedBuilder()
                    .setTitle("✅ Trade erfolgreich!")
                    .setDescription(initiator.getAsMention() + " und " + partner.getAsMention() + " haben getauscht!")
                    .setColor(0x2ECC71);

            successEmbed.addField(
                    initiator.getName() + " erhielt:",
                    wantCard.getName(),
                    true
            );

            successEmbed.addField(
                    partner.getName() + " erhielt:",
                    giveCard.getName(),
                    true
            );

            event.editMessageEmbeds(successEmbed.build())
                    .setComponents()
                    .queue();

        } catch (Exception e) {
            event.reply("❌ Fehler beim Ausführen des Trades: " + e.getMessage())
                    .setEphemeral(true)
                    .queue();
            e.printStackTrace();
        }
    }

    private void saveTradeHistory(TradeOffer offer) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:cards.db");

            String sql = "INSERT INTO trade_history (user1_id, user2_id, user1_card_id, user2_card_id) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, offer.initiatorId);
            pstmt.setString(2, offer.partnerId);
            pstmt.setInt(3, offer.giveCardId);
            pstmt.setInt(4, offer.wantCardId);
            pstmt.executeUpdate();

            pstmt.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper-Klasse für Trade-Angebote
    private static class TradeOffer {
        String initiatorId;
        String partnerId;
        int giveCardId;
        int wantCardId;

        TradeOffer(String initiatorId, String partnerId, int giveCardId, int wantCardId) {
            this.initiatorId = initiatorId;
            this.partnerId = partnerId;
            this.giveCardId = giveCardId;
            this.wantCardId = wantCardId;
        }
    }
}