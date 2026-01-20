package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRarity;
import dev.itsLarss.model.CardRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Map;

/**
 * Profile Command - Zeigt ein komplettes User-Profil
 */
public class ProfileCommand extends ListenerAdapter {

    public CommandData getCommandData() {
        return Commands.slash("profil", "Zeige dein Sammler-Profil")
                .addOption(OptionType.USER, "user", "User dessen Profil du sehen möchtest", false);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("profil")) return;

        User targetUser = event.getOption("user") != null
                ? event.getOption("user").getAsUser()
                : event.getUser();

        String userId = targetUser.getId();
        DatabaseManager db = CardBot.getDatabase();

        // Sammle alle Stats
        Map<Card, Integer> userCards = db.getUserCards(userId);
        int totalCards = db.getTotalCardCount(userId);
        int uniqueCards = db.getUniqueCardCount(userId);
        int coins = db.getUserCoins(userId);
        int totalPossible = CardRegistry.getTotalCardCount();
        double completion = totalPossible > 0 ? (uniqueCards * 100.0) / totalPossible : 0;

        // Zähle nach Seltenheit
        int commonCount = 0, uncommonCount = 0, rareCount = 0;
        int epicCount = 0, legendaryCount = 0, mythicCount = 0;

        for (Map.Entry<Card, Integer> entry : userCards.entrySet()) {
            Card card = entry.getKey();
            int quantity = entry.getValue();

            switch (card.getRarity()) {
                case COMMON: commonCount += quantity; break;
                case UNCOMMON: uncommonCount += quantity; break;
                case RARE: rareCount += quantity; break;
                case EPIC: epicCount += quantity; break;
                case LEGENDARY: legendaryCount += quantity; break;
                case MYTHIC: mythicCount += quantity; break;
            }
        }

        // Finde wertvollste Karte
        Card mostValuable = null;
        for (Card card : userCards.keySet()) {
            if (mostValuable == null ||
                    card.getRarity().getSellPrice() > mostValuable.getRarity().getSellPrice()) {
                mostValuable = card;
            }
        }

        // Berechne Gesamtwert der Sammlung
        int totalValue = 0;
        for (Map.Entry<Card, Integer> entry : userCards.entrySet()) {
            totalValue += entry.getKey().getRarity().getSellPrice() * entry.getValue();
        }

        // Erstelle Embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("👤 " + targetUser.getName() + "'s Profil")
                .setThumbnail(targetUser.getAvatarUrl())
                .setColor(0x9B59B6);

        // Sammlung-Info
        embed.addField(
                "📚 Sammlung",
                "**" + uniqueCards + "**/" + totalPossible + " verschiedene Karten\n" +
                        "**" + totalCards + "** Karten insgesamt\n" +
                        String.format("**%.1f%%** vollständig", completion),
                true
        );

        // Vermögen
        embed.addField(
                "💰 Vermögen",
                "**" + coins + "** Coins\n" +
                        "Sammlungswert: **" + totalValue + "** Coins",
                true
        );

        // Leerzeichen für Layout
        embed.addBlankField(false);

        // Seltenheitsverteilung
        StringBuilder rarityText = new StringBuilder();
        if (commonCount > 0) rarityText.append("⚪ Common: **").append(commonCount).append("**\n");
        if (uncommonCount > 0) rarityText.append("🟢 Uncommon: **").append(uncommonCount).append("**\n");
        if (rareCount > 0) rarityText.append("🔵 Rare: **").append(rareCount).append("**\n");
        if (epicCount > 0) rarityText.append("🟣 Epic: **").append(epicCount).append("**\n");
        if (legendaryCount > 0) rarityText.append("🟠 Legendary: **").append(legendaryCount).append("**\n");
        if (mythicCount > 0) rarityText.append("🟡 Mythic: **").append(mythicCount).append("**\n");

        if (rarityText.length() > 0) {
            embed.addField(
                    "🎴 Kartenverteilung",
                    rarityText.toString(),
                    true
            );
        }

        // Wertvollste Karte
        if (mostValuable != null) {
            embed.addField(
                    "💎 Wertvollste Karte",
                    mostValuable.getRarity().getEmoji() + " **" + mostValuable.getName() + "**\n" +
                            "Wert: **" + mostValuable.getRarity().getSellPrice() + "** Coins",
                    true
            );
        }

        // Zusätzliche Stats
        if (userCards.isEmpty()) {
            embed.setDescription("*Noch keine Karten gesammelt. Nutze `/daily` um zu starten!*");
        } else {
            // Berechne Rang
            String rank = getRank(completion, mythicCount);
            embed.setFooter("Rang: " + rank);
        }

        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Gibt einen Rang basierend auf Fortschritt zurück
     */
    private String getRank(double completion, int mythicCount) {
        if (mythicCount >= 2) return "🌟 Meistersammler";
        if (mythicCount >= 1) return "⭐ Legendärer Sammler";
        if (completion >= 75) return "💎 Experte";
        if (completion >= 50) return "🎯 Fortgeschrittener";
        if (completion >= 25) return "📈 Sammler";
        if (completion >= 10) return "🌱 Anfänger";
        return "🆕 Neuling";
    }
}