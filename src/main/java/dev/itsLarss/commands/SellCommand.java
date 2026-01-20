package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.HashMap;
import java.util.Map;

/**
 * Sell Command - Verkaufe Karten für Coins
 */
public class SellCommand extends ListenerAdapter {

    // Speichert Verkaufs-Angebote: MessageID -> Verkaufs-Info
    private static Map<String, SellOffer> activeSells = new HashMap<>();

    public CommandData getCommandData() {
        return Commands.slash("sell", "Verkaufe eine Karte für Coins")
                .addOption(OptionType.STRING, "karte", "Name der Karte die du verkaufen möchtest", true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("sell")) return;

        String userId = event.getUser().getId();
        String cardName = event.getOption("karte").getAsString();

        // Finde Karte
        Card card = CardRegistry.getCardByName(cardName);

        if (card == null) {
            event.reply("❌ Karte '" + cardName + "' nicht gefunden!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        DatabaseManager db = CardBot.getDatabase();
        int quantity = db.getUserCardQuantity(userId, card.getId());

        if (quantity < 1) {
            event.reply("❌ Du besitzt keine '" + card.getName() + "'!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Berechne Verkaufspreis
        int price = card.getRarity().getSellPrice();

        // Erstelle Bestätigungs-Embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("💸 Karte verkaufen?")
                .setDescription("Möchtest du **" + card.getName() + "** verkaufen?")
                .setColor(0xF39C12);

        embed.addField("Seltenheit", card.getRarity().getEmoji() + " " + card.getRarity().getName(), true);
        embed.addField("Preis", price + " 🪙", true);
        embed.addField("Dein Besitz", quantity + "x", true);

        // Erstelle Buttons
        Button confirmButton = Button.success("sell_confirm", "✅ Verkaufen für " + price + " Coins");
        Button cancelButton = Button.danger("sell_cancel", "❌ Abbrechen");

        event.replyEmbeds(embed.build())
                .addActionRow(confirmButton, cancelButton)
                .queue(message -> {
                    message.retrieveOriginal().queue(msg -> {
                        activeSells.put(msg.getId(), new SellOffer(userId, card.getId(), price));

                        // Timeout nach 30 Sekunden
                        new Thread(() -> {
                            try {
                                Thread.sleep(30000);
                                if (activeSells.containsKey(msg.getId())) {
                                    activeSells.remove(msg.getId());

                                    EmbedBuilder timeoutEmbed = new EmbedBuilder()
                                            .setTitle("⏱️ Verkauf abgelaufen")
                                            .setDescription("Du hast zu lange gewartet.")
                                            .setColor(0x95A5A6);

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

        if (!buttonId.startsWith("sell_")) return;

        String messageId = event.getMessageId();
        SellOffer offer = activeSells.get(messageId);

        if (offer == null) {
            event.reply("❌ Dieses Verkaufsangebot ist nicht mehr verfügbar!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Nur der User selbst kann verkaufen
        if (!event.getUser().getId().equals(offer.userId)) {
            event.reply("❌ Das ist nicht dein Verkauf!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (buttonId.equals("sell_confirm")) {
            // Verkaufe Karte
            DatabaseManager db = CardBot.getDatabase();

            // Prüfe nochmal ob Karte vorhanden
            if (db.getUserCardQuantity(offer.userId, offer.cardId) < 1) {
                event.reply("❌ Du besitzt diese Karte nicht mehr!")
                        .setEphemeral(true)
                        .queue();
                activeSells.remove(messageId);
                return;
            }

            // Entferne Karte und gebe Coins
            db.removeCardFromUser(offer.userId, offer.cardId);
            db.addCoins(offer.userId, offer.price);

            activeSells.remove(messageId);

            Card card = CardRegistry.getCard(offer.cardId);
            int newBalance = db.getUserCoins(offer.userId);

            EmbedBuilder successEmbed = new EmbedBuilder()
                    .setTitle("💰 Karte verkauft!")
                    .setDescription("Du hast **" + card.getName() + "** für **" + offer.price + "** Coins verkauft!")
                    .setColor(0x2ECC71)
                    .setFooter("Neues Guthaben: " + newBalance + " Coins");

            event.editMessageEmbeds(successEmbed.build())
                    .setComponents()
                    .queue();

        } else if (buttonId.equals("sell_cancel")) {
            // Abbrechen
            activeSells.remove(messageId);

            event.editMessage("Verkauf abgebrochen.")
                    .setEmbeds()
                    .setComponents()
                    .queue();
        }
    }

    // Helper-Klasse
    private static class SellOffer {
        String userId;
        int cardId;
        int price;

        SellOffer(String userId, int cardId, int price) {
            this.userId = userId;
            this.cardId = cardId;
            this.price = price;
        }
    }
}