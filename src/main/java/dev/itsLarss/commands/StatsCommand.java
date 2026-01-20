package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRarity;
import dev.itsLarss.model.CardRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.HashMap;
import java.util.Map;

public class StatsCommand extends ListenerAdapter {

    public CommandData getCommandData() {
        return Commands.slash("stats", "Zeige Sammlungsstatistiken");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("stats")) return;

        String userId = event.getUser().getId();
        DatabaseManager db = CardBot.getDatabase();

        Map<Card, Integer> userCards = db.getUserCards(userId);

        if (userCards.isEmpty()) {
            event.reply("📭 Du hast noch keine Karten!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Berechne Statistiken
        int totalCards = db.getTotalCardCount(userId);
        int uniqueCards = db.getUniqueCardCount(userId);
        int totalPossible = CardRegistry.getTotalCardCount();
        double completion = (uniqueCards * 100.0) / totalPossible;

        // Zähle nach Seltenheit
        Map<CardRarity, Integer> rarityCount = new HashMap<>();
        for (CardRarity rarity : CardRarity.values()) {
            rarityCount.put(rarity, 0);
        }

        for (Map.Entry<Card, Integer> entry : userCards.entrySet()) {
            Card card = entry.getKey();
            int quantity = entry.getValue();
            rarityCount.put(card.getRarity(),
                    rarityCount.get(card.getRarity()) + quantity);
        }

        // Erstelle Embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 " + event.getUser().getName() + "'s Statistiken")
                .setColor(0xE74C3C);

        embed.addField(
                "Sammlung",
                "**" + uniqueCards + "**/" + totalPossible + " Karten\n" +
                        String.format("%.1f%% vollständig", completion),
                true
        );

        embed.addField(
                "Insgesamt",
                "**" + totalCards + "** Karten",
                true
        );

        int coins = db.getUserCoins(userId);
        embed.addField(
                "Coins",
                "**" + coins + "** 🪙",
                true
        );

        // Seltenheitsverteilung
        StringBuilder rarityText = new StringBuilder();
        for (CardRarity rarity : CardRarity.values()) {
            int count = rarityCount.get(rarity);
            if (count > 0) {
                rarityText.append(rarity.getEmoji())
                        .append(" ")
                        .append(rarity.getName())
                        .append(": **")
                        .append(count)
                        .append("**\n");
            }
        }

        if (rarityText.length() > 0) {
            embed.addField(
                    "Nach Seltenheit",
                    rarityText.toString(),
                    false
            );
        }

        event.replyEmbeds(embed.build()).queue();
    }
}