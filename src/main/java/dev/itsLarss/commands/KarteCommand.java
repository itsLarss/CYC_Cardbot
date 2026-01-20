package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class KarteCommand extends ListenerAdapter {

    public CommandData getCommandData() {
        return Commands.slash("karte", "Zeige Details zu einer Karte")
                .addOption(OptionType.STRING, "name", "Name der Karte", true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("karte")) return;

        String cardName = event.getOption("name").getAsString();
        Card card = CardRegistry.getCardByName(cardName);

        if (card == null) {
            event.reply("❌ Keine Karte mit dem Namen '" + cardName + "' gefunden!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String userId = event.getUser().getId();
        DatabaseManager db = CardBot.getDatabase();

        int quantity = db.getUserCardQuantity(userId, card.getId());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(card.getRarity().getEmoji() + " " + card.getName())
                .setDescription(card.getDescription())
                .setColor(card.getRarity().getColor())
                .addField("Seltenheit", card.getRarity().getName(), true)
                .addField("Serie", card.getSeries(), true)
                .addField("Dein Besitz", quantity + "x", true)
                .addField("Karten-ID", "#" + card.getId(), true)
                .addField("Verkaufswert", card.getRarity().getSellPrice() + " 🪙", true)
                .setImage(card.getImageUrl());

        event.replyEmbeds(embed.build()).queue();
    }
}