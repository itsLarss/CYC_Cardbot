package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRarity;
import dev.itsLarss.model.CardRegistry;
import dev.itsLarss.util.NSFWManager;
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
import java.util.stream.Collectors;

/**
 * Pack Command mit NSFW-Schutz
 */
public class PackCommand extends ListenerAdapter {

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
    private static Map<String, PackSelection> activeSelections = new HashMap<>();
    private static Map<String, PackPick> activePicks = new HashMap<>();

    public CommandData getCommandData() {
        return Commands.slash("pack", "Öffne ein Kartenpack!");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("pack")) return;

        String userId = event.getUser().getId();
        DatabaseManager db = CardBot.getDatabase();

        // ⭐ NSFW-Check: Bestimme welche Karten verfügbar sind
        boolean isNSFW = NSFWManager.isNSFWChannel(event);

        int coins = db.getUserCoins(userId);

        // Zeige Pack-Auswahl
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📦 Wähle ein Kartenpack!")
                .setDescription("**Dein Guthaben:** " + coins + " 🪙\n" +
                        "**Modus:** " + (isNSFW ? "🔞 NSFW" : "✅ SFW") + "\n\n" +
                        "Wähle eines der folgenden Packs:")
                .setColor(0x3498DB);

        // Normal Pack Info
        embed.addField(
                "📦 Normal Pack - " + PackType.NORMAL.cost + " 🪙",
                "• 5 Karten\n" +
                        "• Standard Drop-Chancen\n",
                true
        );

        // Special Pack Info
        embed.addField(
                "✨ Special Pack - " + PackType.SPECIAL.cost + " 🪙",
                "• 5 Karten\n" +
                        "• **3x höhere Legendary Chance!**\n" +
                        "• **5x höhere Mythic Chance!**\n",
                true
        );

        // ⭐ NSFW-Warnung falls NSFW-Channel
        if (isNSFW) {
            embed.setFooter("⚠️ 18+ Content möglich | Nur für volljährige Nutzer");
        } else {
            embed.setFooter("✅ Sicher für alle Altersgruppen");
        }

        Button normalPack = Button.primary("pack_select_normal",
                "📦 Normal Pack (" + PackType.NORMAL.cost + " 🪙)");
        Button specialPack = Button.success("pack_select_special",
                "✨ Special Pack (" + PackType.SPECIAL.cost + " 🪙)");

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
                        PackSelection selection = new PackSelection(userId, isNSFW);
                        activeSelections.put(msg.getId(), selection);

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

        if (buttonId.startsWith("pack_select_")) {
            handlePackSelection(event, buttonId, messageId);
        } else if (buttonId.startsWith("pack_pick_")) {
            handleCardSelection(event, buttonId, messageId);
        }
    }

    private void handlePackSelection(ButtonInteractionEvent event, String buttonId, String messageId) {
        PackSelection selection = activeSelections.get(messageId);

        if (selection == null) {
            event.reply("❌ Diese Auswahl ist nicht mehr verfügbar!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!event.getUser().getId().equals(selection.userId)) {
            event.reply("❌ Das ist nicht deine Pack-Auswahl!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        PackType packType = buttonId.equals("pack_select_normal")
                ? PackType.NORMAL
                : PackType.SPECIAL;

        DatabaseManager db = CardBot.getDatabase();
        int coins = db.getUserCoins(selection.userId);

        if (coins < packType.cost) {
            event.reply("❌ Du hast nicht genug Coins!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        db.addCoins(selection.userId, -packType.cost);

        // ⭐ Ziehe Karten basierend auf NSFW-Status
        List<Card> drawnCards = drawCardsForPackType(packType, selection.isNSFW);

        activeSelections.remove(messageId);
        showCardSelection(event, drawnCards, packType, selection.isNSFW);
    }

    /**
     * ⭐ Zieht Karten mit NSFW-Filterung
     */
    private List<Card> drawCardsForPackType(PackType packType, boolean includeNSFW) {
        List<Card> cards = new ArrayList<>();
        Set<Integer> drawnIds = new HashSet<>();

        Map<CardRarity, Double> customChances = getCustomDropRates(packType, includeNSFW);

        for (int i = 0; i < CARDS_PER_PACK; i++) {
            Card card = drawCardWithCustomRates(customChances, drawnIds, includeNSFW);
            if (card != null) {
                cards.add(card);
                drawnIds.add(card.getId());
            }
        }

        return cards;
    }

    /**
     * ⭐ Drop-Raten mit NSFW-Support
     */
    private Map<CardRarity, Double> getCustomDropRates(PackType packType, boolean includeNSFW) {
        Map<CardRarity, Double> rates = new HashMap<>();

        if (packType == PackType.SPECIAL) {
            // SFW Rarities
            rates.put(CardRarity.COMMON, 20.0);
            rates.put(CardRarity.UNCOMMON, 15.0);
            rates.put(CardRarity.RARE, 25.0);
            rates.put(CardRarity.EPIC, 15.0);
            rates.put(CardRarity.LEGENDARY, 7.5);
            rates.put(CardRarity.MYTHIC, 2.5);

            // NSFW Rarities (nur wenn NSFW-Channel!)
            if (includeNSFW) {
                rates.put(CardRarity.NSFW_COMMON, 20.0);
                rates.put(CardRarity.NSFW_UNCOMMON, 15.0);
                rates.put(CardRarity.NSFW_RARE, 25.0);
                rates.put(CardRarity.NSFW_EPIC, 15.0);
                rates.put(CardRarity.NSFW_LEGENDARY, 7.5);
                rates.put(CardRarity.NSFW_MYTHIC, 2.5);
            } else {
                // NSFW-Chancen auf 0 setzen!
                rates.put(CardRarity.NSFW_COMMON, 0.0);
                rates.put(CardRarity.NSFW_UNCOMMON, 0.0);
                rates.put(CardRarity.NSFW_RARE, 0.0);
                rates.put(CardRarity.NSFW_EPIC, 0.0);
                rates.put(CardRarity.NSFW_LEGENDARY, 0.0);
                rates.put(CardRarity.NSFW_MYTHIC, 0.0);
            }
        } else {
            // Normal Pack
            rates.put(CardRarity.COMMON, 50.0);
            rates.put(CardRarity.UNCOMMON, 25.0);
            rates.put(CardRarity.RARE, 15.0);
            rates.put(CardRarity.EPIC, 7.0);
            rates.put(CardRarity.LEGENDARY, 2.5);
            rates.put(CardRarity.MYTHIC, 0.5);

            // NSFW Rarities
            if (includeNSFW) {
                rates.put(CardRarity.NSFW_COMMON, 50.0);
                rates.put(CardRarity.NSFW_UNCOMMON, 25.0);
                rates.put(CardRarity.NSFW_RARE, 15.0);
                rates.put(CardRarity.NSFW_EPIC, 7.0);
                rates.put(CardRarity.NSFW_LEGENDARY, 2.5);
                rates.put(CardRarity.NSFW_MYTHIC, 0.5);
            } else {
                // Komplett ausschließen!
                rates.put(CardRarity.NSFW_COMMON, 0.0);
                rates.put(CardRarity.NSFW_UNCOMMON, 0.0);
                rates.put(CardRarity.NSFW_RARE, 0.0);
                rates.put(CardRarity.NSFW_EPIC, 0.0);
                rates.put(CardRarity.NSFW_LEGENDARY, 0.0);
                rates.put(CardRarity.NSFW_MYTHIC, 0.0);
            }
        }

        return rates;
    }

    /**
     * ⭐ Zieht Karte mit NSFW-Filterung
     */
    private Card drawCardWithCustomRates(Map<CardRarity, Double> customChances,
                                         Set<Integer> excludeIds, boolean includeNSFW) {
        // ⭐ Nur verfügbare Karten basierend auf NSFW-Status
        List<Card> availableCards = includeNSFW
                ? new ArrayList<>(CardRegistry.getAllCards())
                : CardRegistry.getSFWCards();

        // Gruppiere nach Seltenheit
        Map<CardRarity, List<Card>> cardsByRarity = new HashMap<>();
        for (CardRarity rarity : CardRarity.values()) {
            cardsByRarity.put(rarity, new ArrayList<>());
        }

        for (Card card : availableCards) {
            if (!excludeIds.contains(card.getId())) {
                // ⭐ Doppelte Sicherheit: Keine NSFW in SFW!
                if (!includeNSFW && card.getRarity().isNSFW()) {
                    continue;
                }
                cardsByRarity.get(card.getRarity()).add(card);
            }
        }

        // Ziehe Seltenheit
        double rand = ThreadLocalRandom.current().nextDouble(100);
        double cumulative = 0;
        CardRarity selectedRarity = CardRarity.COMMON;

        for (CardRarity rarity : CardRarity.values()) {
            Double chance = customChances.get(rarity);
            if (chance == null || chance == 0.0) continue;

            cumulative += chance;
            if (rand <= cumulative) {
                selectedRarity = rarity;
                break;
            }
        }

        // Hole Karten dieser Seltenheit
        List<Card> cardsOfRarity = cardsByRarity.get(selectedRarity);

        // Fallbacks
        if (cardsOfRarity.isEmpty()) {
            cardsOfRarity = new ArrayList<>(CardRegistry.getCardsByRarity(selectedRarity));
            // ⭐ Filter NSFW wieder raus falls SFW-Modus!
            if (!includeNSFW) {
                cardsOfRarity = cardsOfRarity.stream()
                        .filter(c -> !c.getRarity().isNSFW())
                        .collect(Collectors.toList());
            }
        }

        if (cardsOfRarity.isEmpty()) {
            // Nimm irgendeine verfügbare SFW-Karte
            List<Card> fallbackCards = includeNSFW
                    ? new ArrayList<>(CardRegistry.getAllCards())
                    : CardRegistry.getSFWCards();

            if (!fallbackCards.isEmpty()) {
                return fallbackCards.get(ThreadLocalRandom.current().nextInt(fallbackCards.size()));
            }
            return null;
        }

        return cardsOfRarity.get(ThreadLocalRandom.current().nextInt(cardsOfRarity.size()));
    }

    private void showCardSelection(ButtonInteractionEvent event, List<Card> drawnCards,
                                   PackType packType, boolean isNSFW) {
        List<MessageEmbed> embeds = new ArrayList<>();

        EmbedBuilder mainEmbed = new EmbedBuilder()
                .setTitle("✨ " + packType.name + " geöffnet!")
                .setDescription("**Wähle 1 von 5 Karten:**\n" +
                        "Modus: " + (isNSFW ? "🔞 NSFW" : "✅ SFW"))
                .setColor(packType.color)
                .setFooter("Du hast 60 Sekunden Zeit zu wählen!");

        embeds.add(mainEmbed.build());

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

            // ⭐ NSFW-Warnung auf Karte
            if (card.getRarity().isNSFW()) {
                cardEmbed.setFooter("🔞 Nur für Erwachsene | 18+");
            }

            if (card.hasImage()) {
                cardEmbed.setImage(card.getImageUrl());
            }

            embeds.add(cardEmbed.build());
        }

        List<Button> buttons = new ArrayList<>();
        for (int i = 0; i < drawnCards.size(); i++) {
            Card card = drawnCards.get(i);
            String buttonLabel = numberEmojis[i] + " " + card.getName();
            // ⭐ NSFW-Emoji auf Button
            if (card.getRarity().isNSFW()) {
                buttonLabel = "🔞 " + buttonLabel;
            }
            buttons.add(Button.primary("pack_pick_" + i, buttonLabel));
        }

        event.editMessageEmbeds(embeds)
                .setActionRow(buttons)
                .queue();

        event.getHook().retrieveOriginal().queue(msg -> {
            PackPick pick = new PackPick(event.getUser().getId(), drawnCards, packType);
            activePicks.put(msg.getId(), pick);

            new Thread(() -> {
                try {
                    Thread.sleep(60000);
                    if (activePicks.containsKey(msg.getId())) {
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

    private void handleCardSelection(ButtonInteractionEvent event, String buttonId, String messageId) {
        PackPick pick = activePicks.get(messageId);

        if (pick == null) {
            event.reply("❌ Diese Auswahl ist nicht mehr verfügbar!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!event.getUser().getId().equals(pick.userId)) {
            event.reply("❌ Das ist nicht dein Pack!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        int chosenIndex = Integer.parseInt(buttonId.replace("pack_pick_", ""));
        Card chosenCard = pick.cards.get(chosenIndex);

        DatabaseManager db = CardBot.getDatabase();
        db.addCardToUser(pick.userId, chosenCard.getId());

        activePicks.remove(messageId);

        EmbedBuilder successEmbed = new EmbedBuilder()
                .setTitle("✅ Karte erhalten aus " + pick.packType.name + "!")
                .setDescription("Du hast **" + chosenCard.getName() + "** gewählt!")
                .setColor(chosenCard.getRarity().getColor())
                .addField("Seltenheit", chosenCard.getRarity().getEmoji() + " " + chosenCard.getRarity().getName(), true)
                .addField("Serie", chosenCard.getSeries(), true)
                .addField("Beschreibung", chosenCard.getDescription(), false);

        if (chosenCard.hasImage()) {
            successEmbed.setImage(chosenCard.getImageUrl());
        }

        // ⭐ NSFW-Warnung
        if (chosenCard.getRarity().isNSFW()) {
            successEmbed.setFooter("🔞 NSFW-Karte | Nur für Erwachsene (18+)");
        }

        int newBalance = db.getUserCoins(pick.userId);
        successEmbed.addField("Guthaben", newBalance + " 🪙", false);

        event.editMessageEmbeds(successEmbed.build())
                .setComponents()
                .queue();
    }

    private static class PackSelection {
        String userId;
        boolean isNSFW;

        PackSelection(String userId, boolean isNSFW) {
            this.userId = userId;
            this.isNSFW = isNSFW;
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