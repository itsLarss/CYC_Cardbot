package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Gift Command - Verschenke Karten an andere User
 */
public class GiftCommand extends ListenerAdapter {

    public CommandData getCommandData() {
        return Commands.slash("gift", "Verschenke eine Karte an einen anderen User")
                .addOption(OptionType.USER, "user", "An wen verschenken?", true)
                .addOption(OptionType.STRING, "karte", "Welche Karte verschenken?", true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("gift")) return;

        User giver = event.getUser();
        User receiver = event.getOption("user").getAsUser();
        String cardName = event.getOption("karte").getAsString();

        // Validierung: Nicht an sich selbst
        if (receiver.getId().equals(giver.getId())) {
            event.reply("❌ Du kannst dir nicht selbst Karten schenken!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Validierung: Nicht an Bots
        if (receiver.isBot()) {
            event.reply("❌ Du kannst Bots keine Karten schenken!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Finde Karte
        Card card = CardRegistry.getCardByName(cardName);

        if (card == null) {
            event.reply("❌ Karte '" + cardName + "' nicht gefunden!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        DatabaseManager db = CardBot.getDatabase();

        // Prüfe ob Geber die Karte besitzt
        if (db.getUserCardQuantity(giver.getId(), card.getId()) < 1) {
            event.reply("❌ Du besitzt keine '" + card.getName() + "'!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Führe Geschenk aus
        db.removeCardFromUser(giver.getId(), card.getId());
        db.addCardToUser(receiver.getId(), card.getId());

        // Erstelle Embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎁 Karte verschenkt!")
                .setDescription(giver.getAsMention() + " hat " + receiver.getAsMention() + " eine Karte geschenkt!")
                .setColor(0xFF69B4);

        embed.addField(
                "Geschenkte Karte",
                card.getRarity().getEmoji() + " **" + card.getName() + "**\n" +
                        "*" + card.getRarity().getName() + "* - " + card.getDescription(),
                false
        );

        embed.setFooter("Wie großzügig! ❤️");

        event.replyEmbeds(embed.build()).queue();
    }
}