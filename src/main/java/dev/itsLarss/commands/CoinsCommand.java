package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import dev.itsLarss.database.DatabaseManager;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CoinsCommand extends ListenerAdapter {

    public CommandData getCommandData() {
        return Commands.slash("coins", "Zeige dein Coin-Guthaben")
                .addOption(OptionType.USER, "user", "User dessen Guthaben du sehen möchtest", false);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("coins")) return;

        User targetUser = event.getOption("user") != null
                ? event.getOption("user").getAsUser()
                : event.getUser();

        String userId = targetUser.getId();
        DatabaseManager db = CardBot.getDatabase();

        int coins = db.getUserCoins(userId);

        event.reply("💰 " + targetUser.getName() + " hat **" + coins + "** Coins!")
                .queue();
    }
}