package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRarity;
import dev.itsLarss.model.CardRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Pack Command mit 2 Pack-Typen:
 * - Normal Pack (100 Coins) - Standard Drop Rates
 * - Special Pack (1000 Coins) - Erhöhte Legendary/Mythic Chancen
 */
public class PackCommand extends ListenerAdapter {

    // Pack-Typen
    private enum PackType {
        NORMAL(100, "Normal Pack", 0x9C27B0),
        SPECIAL(1000, "Special Pack", 0xFFD700);

        final int cost;
        final String name;
        final int color;

        PackType(int cost, String name, int color) {
            this.cost = cost;
            this.name = name;
            this.color = color;
        }
    }

    private static final int CARDS_PER_PACK = 5;

    // Speichert aktive Pack-Auswahlen: MessageID -> PackSelection
    private static Map<String, PackSelection> activeSelections = new HashMap<>();

    // Speichert aktive Card-Picks: MessageID -> PackPick
    private static Map<String, PackPick> activePicks = new HashMap<>();

    public CommandData getCommandData() {
        return Commands.slash("pack", "Öffne ein Kartenpack!");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("pack")) return;

        String userId = event.getUser().getId();
        DatabaseManager db = CardBot.getDatabase();

        int coins = db.getUserCoins(userId);

        // Zeige Pack-Auswahl
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📦 Wähle ein Kartenpack!")
                .setDescription("**Dein Guthaben:** " + coins + " 🪙\n\n" +
                        "Wähle eines der folgenden Packs:")
                .setColor(0x3498DB);

        // Normal Pack Info
        embed.addField(
                "📦 Normal Pack - " + PackType.NORMAL.cost + " 🪙",
                "• 5 Karten\n" +
                        "• Standard Drop-Chancen\n" +
                        "• Günstig für viele Öffnungen",
                true
        );

        // Special Pack Info
        embed.addField(
                "✨ Special Pack - " + PackType.SPECIAL.cost + " 🪙",
                "• 5 Karten\n" +
                        "• **3x höhere Legendary Chance!**\n" +
                        "• **5x höhere Mythic Chance!**\n" +
                        "• Garantiert mindestens Rare!",
                true
        );

        // Drop-Rate Vergleich
        embed.addField(
                "📊 Drop-Raten Vergleich",
                "```\n" +
                        "Seltenheit    Normal    Special\n" +
                        "Common        50.0%     25.0%\n" +
                        "Uncommon      25.0%     20.0%\n" +
                        "Rare          15.0%     25.0%\n" +
                        "Epic           7.0%     15.0%\n" +
                        "Legendary      2.5%      7.5%  ⭐\n" +
                        "Mythic         0.5%      2.5%  ⭐⭐\n" +
                        "```",
                false
        );

        // Erstelle Buttons
        Button normalPack = Button.primary("pack_select_normal",
                "📦 Normal Pack (" + PackType.NORMAL.cost + " 🪙)");
        Button specialPack = Button.success("pack_select_special",
                "✨ Special Pack (" + PackType.SPECIAL.cost + " 🪙)");

        // Deaktiviere Buttons wenn nicht genug Coins
        if (coins < PackType.NORMAL.cost) {
            normalPack = normalPack.asDisabled();
        }
        if (coins < PackType.SPECIAL.cost) {
            specialPack = specialPack.asDisabled();
        }

        event.replyEmbeds(embed.build())
                .addActionRow(normalPack, specialPack)
                .queue(message -> {
                    message.retrieveOriginal().queue(msg -> {
                        PackSelection selection = new PackSelection(userId);
                        activeSelections.put(msg.getId(), selection);

                        // Timeout nach 60 Sekunden
                        new Thread(() -> {
                            try {
                                Thread.sleep(60000);
                                activeSelections.remove(msg.getId());
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
        String messageId = event.getMessageId();

        // Pack-Typ Auswahl
        if (buttonId.startsWith("pack_select_")) {
            handlePackSelection(event, buttonId, messageId);
        }
        // Karten-Auswahl
        else if (buttonId.startsWith("pack_pick_")) {
            handleCardSelection(event, buttonId, messageId);
        }
    }

    /**
     * Behandelt die Pack-Typ Auswahl
     */
    private void handlePackSelection(ButtonInteractionEvent event, String buttonId, String messageId) {
        PackSelection selection = activeSelections.get(messageId);

        if (selection == null) {
            event.reply("❌ Diese Auswahl ist nicht mehr verfügbar!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Nur der richtige User
        if (!event.getUser().getId().equals(selection.userId)) {
            event.reply("❌ Das ist nicht deine Pack-Auswahl!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Bestimme Pack-Typ
        PackType packType = buttonId.equals("pack_select_normal")
                ? PackType.NORMAL
                : PackType.SPECIAL;

        DatabaseManager db = CardBot.getDatabase();
        int coins = db.getUserCoins(selection.userId);

        // Prüfe Coins
        if (coins < packType.cost) {
            event.reply("❌ Du hast nicht genug Coins! " +
                            "Du brauchst **" + packType.cost + "** 🪙, hast aber nur **" + coins + "** 🪙")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Ziehe Coins ab
        db.addCoins(selection.userId, -packType.cost);

        // Ziehe 5 Karten mit angepassten Drop-Rates
        List<Card> drawnCards = drawCardsForPackType(packType);

        // Entferne alte Selection
        activeSelections.remove(messageId);

        // Zeige gezogene Karten
        showCardSelection(event, drawnCards, packType);
    }

    /**
     * Zieht Karten mit angepassten Drop-Rates basierend auf Pack-Typ
     */
    private List<Card> drawCardsForPackType(PackType packType) {
        List<Card> cards = new ArrayList<>();
        Set<Integer> drawnIds = new HashSet<>();

        // Angepasste Drop-Chancen
        Map<CardRarity, Double> customChances = getCustomDropRates(packType);

        for (int i = 0; i < CARDS_PER_PACK; i++) {
            Card card = drawCardWithCustomRates(customChances, drawnIds);
            cards.add(card);
            drawnIds.add(card.getId());
        }

        return cards;
    }

    /**
     * Gibt die angepassten Drop-Raten für den Pack-Typ zurück
     */
    private Map<CardRarity, Double> getCustomDropRates(PackType packType) {
        Map<CardRarity, Double> rates = new HashMap<>();

        if (packType == PackType.SPECIAL) {
            // Special Pack: Erhöhte Legendary/Mythic Chancen
            rates.put(CardRarity.COMMON, 25.0);      // Reduziert von 50%
            rates.put(CardRarity.UNCOMMON, 20.0);    // Reduziert von 25%
            rates.put(CardRarity.RARE, 25.0);        // Erhöht von 15%
            rates.put(CardRarity.EPIC, 15.0);        // Erhöht von 7%
            rates.put(CardRarity.LEGENDARY, 7.5);    // 3x höher! (von 2.5%)
            rates.put(CardRarity.MYTHIC, 2.5);       // 5x höher! (von 0.5%)
        } else {
            // Normal Pack: Standard Chancen
            rates.put(CardRarity.COMMON, 50.0);
            rates.put(CardRarity.UNCOMMON, 25.0);
            rates.put(CardRarity.RARE, 15.0);
            rates.put(CardRarity.EPIC, 7.0);
            rates.put(CardRarity.LEGENDARY, 2.5);
            rates.put(CardRarity.MYTHIC, 0.5);
        }

        return rates;
    }

    /**
     * Zieht eine Karte mit angepassten Drop-Raten
     */
    private Card drawCardWithCustomRates(Map<CardRarity, Double> customChances, Set<Integer> excludeIds) {
        // Gruppiere verfügbare Karten nach Seltenheit
        Map<CardRarity, List<Card>> cardsByRarity = new HashMap<>();
        for (CardRarity rarity : CardRarity.values()) {
            cardsByRarity.put(rarity, new ArrayList<>());
        }

        for (Card card : CardRegistry.getAllCards()) {
            if (!excludeIds.contains(card.getId())) {
                cardsByRarity.get(card.getRarity()).add(card);
            }
        }

        // Ziehe Seltenheit basierend auf Custom-Chancen
        double rand = ThreadLocalRandom.current().nextDouble(100);
        double cumulative = 0;
        CardRarity selectedRarity = CardRarity.COMMON;

        for (CardRarity rarity : CardRarity.values()) {
            cumulative += customChances.get(rarity);
            if (rand <= cumulative) {
                selectedRarity = rarity;
                break;
            }
        }

        // Ziehe zufällige Karte der Seltenheit
        List<Card> availableCards = cardsByRarity.get(selectedRarity);

        // Fallback 1: Alle Karten dieser Seltenheit (auch wenn schon gezogen)
        if (availableCards.isEmpty()) {
            availableCards = new ArrayList<>(CardRegistry.getCardsByRarity(selectedRarity));
        }

        // Fallback 2: Wenn immer noch keine Karten -> Probiere andere Seltenheiten
        if (availableCards.isEmpty()) {
            // Versuche eine Seltenheit niedriger
            for (int i = selectedRarity.ordinal() - 1; i >= 0; i--) {
                CardRarity fallbackRarity = CardRarity.values()[i];
                availableCards = cardsByRarity.get(fallbackRarity);
                if (!availableCards.isEmpty()) {
                    break;
                }
                // Falls auch nicht verfügbar, versuche alle Karten dieser Seltenheit
                availableCards = new ArrayList<>(CardRegistry.getCardsByRarity(fallbackRarity));
                if (!availableCards.isEmpty()) {
                    break;
                }
            }
        }

        // Fallback 3: Wenn IMMER NOCH leer -> Nimm irgendeine verfügbare Karte
        if (availableCards.isEmpty()) {
            for (Card card : CardRegistry.getAllCards()) {
                if (!excludeIds.contains(card.getId())) {
                    availableCards.add(card);
                }
            }
        }

        // Final Fallback: Wenn komplett leer, nimm einfach irgendeine Karte (sollte nie passieren!)
        if (availableCards.isEmpty()) {
            List<Card> allCards = new ArrayList<>(CardRegistry.getAllCards());
            if (!allCards.isEmpty()) {
                return allCards.get(ThreadLocalRandom.current().nextInt(allCards.size()));
            }
            // Wenn GAR KEINE Karten registriert sind, werfe Exception
            throw new IllegalStateException("Keine Karten im Registry gefunden! Bitte füge Karten in CardRegistry.java hinzu.");
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(availableCards.size());
        return availableCards.get(randomIndex);
    }

    /**
     * Zeigt die Karten-Auswahl an
     */
    private void showCardSelection(ButtonInteractionEvent event, List<Card> drawnCards, PackType packType) {
        // Erstelle mehrere Embeds
        List<MessageEmbed> embeds = new ArrayList<>();

        // Haupt-Embed
        EmbedBuilder mainEmbed = new EmbedBuilder()
                .setTitle("✨ " + packType.name + " geöffnet!")
                .setDescription("**Wähle 1 von 5 Karten:**")
                .setColor(packType.color)
                .setFooter("Du hast 60 Sekunden Zeit zu wählen!");

        embeds.add(mainEmbed.build());

        // Ein Embed pro Karte
        String[] numberEmojis = {"1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣"};

        for (int i = 0; i < drawnCards.size(); i++) {
            Card card = drawnCards.get(i);

            EmbedBuilder cardEmbed = new EmbedBuilder()
                    .setTitle(numberEmojis[i] + " " + card.getName())
                    .setDescription(
                            "**" + card.getRarity().getEmoji() + " " + card.getRarity().getName() + "**\n" +
                                    "*" + card.getDescription() + "*"
                    )
                    .setColor(card.getRarity().getColor());

            // Zeige Bild falls vorhanden
            if (card.hasImage()) {
                cardEmbed.setImage(card.getImageUrl());
            }

            embeds.add(cardEmbed.build());
        }

        // Erstelle Auswahl-Buttons
        List<Button> buttons = new ArrayList<>();
        for (int i = 0; i < drawnCards.size(); i++) {
            Card card = drawnCards.get(i);
            buttons.add(Button.primary("pack_pick_" + i,
                    numberEmojis[i] + " " + card.getName()));
        }

        event.editMessageEmbeds(embeds)
                .setActionRow(buttons)
                .queue();

        // Hole Message ID für Timeout
        event.getHook().retrieveOriginal().queue(msg -> {
            // Speichere Pick-Info
            PackPick pick = new PackPick(event.getUser().getId(), drawnCards, packType);
            activePicks.put(msg.getId(), pick);

            // Timeout nach 60 Sekunden
            new Thread(() -> {
                try {
                    Thread.sleep(60000);
                    if (activePicks.containsKey(msg.getId())) {
                        // Automatisch erste Karte wählen
                        Card autoCard = drawnCards.get(0);
                        DatabaseManager db = CardBot.getDatabase();
                        db.addCardToUser(event.getUser().getId(), autoCard.getId());
                        activePicks.remove(msg.getId());

                        EmbedBuilder timeoutEmbed = new EmbedBuilder()
                                .setTitle("⏱️ Zeit abgelaufen!")
                                .setDescription("Du hast automatisch **" + autoCard.getName() + "** erhalten!")
                                .setColor(0x95A5A6);

                        if (autoCard.hasImage()) {
                            timeoutEmbed.setImage(autoCard.getImageUrl());
                        }

                        int newBalance = db.getUserCoins(event.getUser().getId());
                        timeoutEmbed.setFooter("Aktuelles Guthaben: " + newBalance + " 🪙");

                        // Hier ist der Fix! Nutze message.editMessageEmbeds statt msg.editMessageEmbeds
                        msg.editMessageEmbeds(timeoutEmbed.build())
                                .setComponents()
                                .queue();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    /**
     * Behandelt die Karten-Auswahl
     */
    private void handleCardSelection(ButtonInteractionEvent event, String buttonId, String messageId) {
        PackPick pick = activePicks.get(messageId);

        if (pick == null) {
            event.reply("❌ Diese Auswahl ist nicht mehr verfügbar!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Nur der richtige User
        if (!event.getUser().getId().equals(pick.userId)) {
            event.reply("❌ Das ist nicht dein Pack!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Hole gewählten Index
        int chosenIndex = Integer.parseInt(buttonId.replace("pack_pick_", ""));
        Card chosenCard = pick.cards.get(chosenIndex);

        // Füge Karte hinzu
        DatabaseManager db = CardBot.getDatabase();
        db.addCardToUser(pick.userId, chosenCard.getId());

        activePicks.remove(messageId);

        // Zeige Erfolg
        EmbedBuilder successEmbed = new EmbedBuilder()
                .setTitle("✅ Karte erhalten aus " + pick.packType.name + "!")
                .setDescription("Du hast **" + chosenCard.getName() + "** gewählt!")
                .setColor(chosenCard.getRarity().getColor())
                .addField("Seltenheit", chosenCard.getRarity().getEmoji() + " " + chosenCard.getRarity().getName(), true)
                .addField("Serie", chosenCard.getSeries(), true)
                .addField("Beschreibung", chosenCard.getDescription(), false);

        // Zeige Bild
        if (chosenCard.hasImage()) {
            successEmbed.setImage(chosenCard.getImageUrl());
        }

        int newBalance = db.getUserCoins(pick.userId);
        successEmbed.setFooter("Aktuelles Guthaben: " + newBalance + " 🪙");

        event.editMessageEmbeds(successEmbed.build())
                .setComponents()
                .queue();
    }

    // Helper-Klassen
    private static class PackSelection {
        String userId;

        PackSelection(String userId) {
            this.userId = userId;
        }
    }

    private static class PackPick {
        String userId;
        List<Card> cards;
        PackType packType;

        PackPick(String userId, List<Card> cards, PackType packType) {
            this.userId = userId;
            this.cards = cards;
            this.packType = packType;
        }
    }
}