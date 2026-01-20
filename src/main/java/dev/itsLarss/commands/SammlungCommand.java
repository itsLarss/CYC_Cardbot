package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRarity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.*;

public class SammlungCommand extends ListenerAdapter {

    public CommandData getCommandData() {
        return Commands.slash("sammlung", "Zeige deine Kartensammlung")
                .addOption(OptionType.USER, "user", "User dessen Sammlung du sehen möchtest", false);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("sammlung")) return;

        // Bestimme den Ziel-User
        User targetUser = event.getOption("user") != null
                ? event.getOption("user").getAsUser()
                : event.getUser();

        String userId = targetUser.getId();
        DatabaseManager db = CardBot.getDatabase();

        Map<Card, Integer> userCards = db.getUserCards(userId);

        if (userCards.isEmpty()) {
            event.reply("📭 " + targetUser.getName() + " hat noch keine Karten gesammelt!")
                    .queue();
            return;
        }

        // Gruppiere nach Seltenheit
        Map<CardRarity, List<Map.Entry<Card, Integer>>> cardsByRarity = new LinkedHashMap<>();
        for (CardRarity rarity : CardRarity.values()) {
            cardsByRarity.put(rarity, new ArrayList<>());
        }

        int totalCards = 0;
        for (Map.Entry<Card, Integer> entry : userCards.entrySet()) {
            Card card = entry.getKey();
            int quantity = entry.getValue();
            cardsByRarity.get(card.getRarity()).add(entry);
            totalCards += quantity;
        }

        // Erstelle Embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📚 " + targetUser.getName() + "'s Sammlung")
                .setDescription("Insgesamt **" + totalCards + "** Karten | **" + userCards.size() + "** verschiedene")
                .setColor(0x3498DB);

        // Sortiere nach Seltenheit (von selten zu häufig)
        CardRarity[] rarityOrder = {
                CardRarity.MYTHIC, CardRarity.LEGENDARY, CardRarity.EPIC,
                CardRarity.RARE, CardRarity.UNCOMMON, CardRarity.COMMON
        };

        for (CardRarity rarity : rarityOrder) {
            List<Map.Entry<Card, Integer>> cards = cardsByRarity.get(rarity);

            if (!cards.isEmpty()) {
                StringBuilder cardsText = new StringBuilder();

                int displayLimit = 10;
                for (int i = 0; i < Math.min(cards.size(), displayLimit); i++) {
                    Map.Entry<Card, Integer> entry = cards.get(i);
                    cardsText.append("• ")
                            .append(entry.getKey().getName())
                            .append(" x")
                            .append(entry.getValue())
                            .append("\n");
                }

                if (cards.size() > displayLimit) {
                    cardsText.append("... und ")
                            .append(cards.size() - displayLimit)
                            .append(" weitere\n");
                }

                embed.addField(
                        rarity.getEmoji() + " " + rarity.getName() + " (" + cards.size() + ")",
                        cardsText.toString(),
                        false
                );
            }
        }

        int coins = db.getUserCoins(userId);
        embed.setFooter("💰 " + coins + " Coins");

        event.replyEmbeds(embed.build()).queue();
    }
}