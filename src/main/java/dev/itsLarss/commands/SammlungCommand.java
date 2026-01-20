package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.model.Card;
import dev.itsLarss.util.NSFWManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Map;

/**
 * Sammlung Command mit NSFW-Filterung
 */
public class SammlungCommand extends ListenerAdapter {

    public CommandData getCommandData() {
        return Commands.slash("sammlung", "Zeigt deine Kartensammlung");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("sammlung")) return;

        String userId = event.getUser().getId();
        DatabaseManager db = CardBot.getDatabase();

        // ⭐ NSFW-Check
        boolean isNSFW = NSFWManager.isNSFWChannel(event);

        Map<Card, Integer> userCards = db.getUserCards(userId);

        if (userCards.isEmpty()) {
            event.reply("📭 Deine Sammlung ist leer!\nNutze `/daily` oder `/pack` um Karten zu erhalten.")
                    .queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎴 Deine Kartensammlung")
                .setDescription("**Modus:** " + (isNSFW ? "🔞 NSFW" : "✅ SFW") + "\n" +
                        (isNSFW ? "" : "*(NSFW-Karten werden nicht angezeigt)*"))
                .setColor(0x3498DB);

        int totalCards = 0;
        int hiddenNSFWCards = 0;

        for (Map.Entry<Card, Integer> entry : userCards.entrySet()) {
            Card card = entry.getKey();
            int quantity = entry.getValue();

            // ⭐ Filter NSFW-Karten in SFW-Channels!
            if (!isNSFW && card.getRarity().isNSFW()) {
                hiddenNSFWCards += quantity;
                continue; // NICHT anzeigen!
            }

            totalCards += quantity;

            String fieldName = card.getRarity().getEmoji() + " " + card.getName();
            String fieldValue = "**" + card.getRarity().getName() + "**\n" +
                    "Anzahl: " + quantity + "x\n" +
                    "Wert: " + (card.getRarity().getSellValue() * quantity) + " 🪙";

            embed.addField(fieldName, fieldValue, true);
        }

        embed.setFooter("Gesamt: " + totalCards + " Karten" +
                (hiddenNSFWCards > 0 ? " | " + hiddenNSFWCards + " NSFW-Karten versteckt 🔞" : ""));

        event.replyEmbeds(embed.build()).queue();
    }
}