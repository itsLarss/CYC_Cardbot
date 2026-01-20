package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DailyCommand extends ListenerAdapter {

    private static Map<String, DailyPick> activePicks = new HashMap<>();

    public CommandData getCommandData() {
        return Commands.slash("daily", "Hole deine tägliche Belohnung!");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("daily")) return;

        String userId = event.getUser().getId();
        DatabaseManager db = CardBot.getDatabase();

        // Prüfe ob User claimen kann
        if (!db.canClaimDaily(userId)) {
            long secondsLeft = db.getTimeUntilNextDaily(userId);
            long hours = secondsLeft / 3600;
            long minutes = (secondsLeft % 3600) / 60;

            event.reply("❌ Du hast heute schon geclaimed! " +
                            "Nächster Claim in: **" + hours + "h " + minutes + "m**")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Ziehe 3 verschiedene Karten
        List<Card> drawnCards = CardRegistry.drawMultipleUniqueCards(3);

        // Gebe Coins
        int coins = ThreadLocalRandom.current().nextInt(50, 151);
        db.addCoins(userId, coins);

        // Erstelle MEHRERE Embeds - eines pro Karte!
        List<MessageEmbed> embeds = new ArrayList<>();

        // Haupt-Embed (oben)
        EmbedBuilder mainEmbed = new EmbedBuilder()
                .setTitle("🎁 Daily Belohnung!")
                .setDescription("Du hast **" + coins + "** 🪙 Coins erhalten!\n\n" +
                        "**Wähle 1 der 3 Karten:**")
                .setColor(0x00FF00)
                .setFooter("Du hast 60 Sekunden Zeit zu wählen!");

        embeds.add(mainEmbed.build());

        // Erstelle für jede Karte ein eigenes Embed mit Bild
        String[] numberEmojis = {"1️⃣", "2️⃣", "3️⃣"};

        for (int i = 0; i < drawnCards.size(); i++) {
            Card card = drawnCards.get(i);

            EmbedBuilder cardEmbed = new EmbedBuilder()
                    .setTitle(numberEmojis[i] + " " + card.getName())
                    .setDescription(
                            "**" + card.getRarity().getEmoji() + " " + card.getRarity().getName() + "**\n" +
                                    "*" + card.getDescription() + "*\n" +
                                    "Serie: " + card.getSeries()
                    )
                    .setColor(card.getRarity().getColor());

            // Zeige Bild falls vorhanden
            if (card.hasImage()) {
                cardEmbed.setImage(card.getImageUrl());
            } else {
                // Fallback: Zeige Thumbnail oder nichts
                cardEmbed.setDescription(
                        cardEmbed.getDescriptionBuilder().toString() +
                                "\n\n🖼️ *Kein Bild verfügbar*"
                );
            }

            embeds.add(cardEmbed.build());
        }

        // Erstelle Auswahl-Buttons
        Button card1 = Button.primary("daily_pick_0", "1️⃣ Wähle " + drawnCards.get(0).getName());
        Button card2 = Button.primary("daily_pick_1", "2️⃣ Wähle " + drawnCards.get(1).getName());
        Button card3 = Button.primary("daily_pick_2", "3️⃣ Wähle " + drawnCards.get(2).getName());

        // Sende ALLE Embeds auf einmal (max 10 Embeds erlaubt!)
        event.replyEmbeds(embeds)
                .addActionRow(card1, card2, card3)
                .queue(message -> {
                    message.retrieveOriginal().queue(msg -> {
                        // Speichere Pick-Info
                        DailyPick pick = new DailyPick(userId, drawnCards);
                        activePicks.put(msg.getId(), pick);

                        // Timeout nach 60 Sekunden
                        new Thread(() -> {
                            try {
                                Thread.sleep(60000);
                                if (activePicks.containsKey(msg.getId())) {
                                    // Automatisch erste Karte wählen
                                    Card autoCard = drawnCards.get(0);
                                    db.addCardToUser(userId, autoCard.getId());
                                    db.setDailyClaim(userId);
                                    activePicks.remove(msg.getId());

                                    EmbedBuilder timeoutEmbed = new EmbedBuilder()
                                            .setTitle("⏱️ Zeit abgelaufen!")
                                            .setDescription("Du hast automatisch **" + autoCard.getName() + "** erhalten!")
                                            .setColor(0x95A5A6);

                                    if (autoCard.hasImage()) {
                                        timeoutEmbed.setImage(autoCard.getImageUrl());
                                    }

                                    msg.editMessageEmbeds(timeoutEmbed.build())
                                            .setComponents()
                                            .queue();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();

        if (!buttonId.startsWith("daily_pick_")) return;

        String messageId = event.getMessageId();
        DailyPick pick = activePicks.get(messageId);

        if (pick == null) {
            event.reply("❌ Diese Auswahl ist nicht mehr verfügbar!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Nur der richtige User kann wählen
        if (!event.getUser().getId().equals(pick.userId)) {
            event.reply("❌ Das ist nicht deine Daily-Belohnung!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Hole gewählten Index
        int chosenIndex = Integer.parseInt(buttonId.replace("daily_pick_", ""));
        Card chosenCard = pick.cards.get(chosenIndex);

        // Füge Karte hinzu und setze Daily
        DatabaseManager db = CardBot.getDatabase();
        db.addCardToUser(pick.userId, chosenCard.getId());
        db.setDailyClaim(pick.userId);

        activePicks.remove(messageId);

        // Zeige Erfolg
        EmbedBuilder successEmbed = new EmbedBuilder()
                .setTitle("✅ Karte erhalten!")
                .setDescription("Du hast **" + chosenCard.getName() + "** gewählt!")
                .setColor(chosenCard.getRarity().getColor())
                .addField("Seltenheit", chosenCard.getRarity().getEmoji() + " " + chosenCard.getRarity().getName(), true)
                .addField("Serie", chosenCard.getSeries(), true)
                .addField("Beschreibung", chosenCard.getDescription(), false);

        // Zeige das große Bild der gewählten Karte
        if (chosenCard.hasImage()) {
            successEmbed.setImage(chosenCard.getImageUrl());
        }

        int totalCoins = db.getUserCoins(pick.userId);
        successEmbed.setFooter("Gesamtguthaben: " + totalCoins + " 🪙");

        event.editMessageEmbeds(successEmbed.build())
                .setComponents()
                .queue();
    }

    // Helper-Klasse
    private static class DailyPick {
        String userId;
        List<Card> cards;

        DailyPick(String userId, List<Card> cards) {
            this.userId = userId;
            this.cards = cards;
        }
    }
}