package dev.itsLarss.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class HelpCommand extends ListenerAdapter {

    public CommandData getCommandData() {
        return Commands.slash("help", "Zeige alle verfügbaren Befehle");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("help")) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎴 Kartensammel-Bot Hilfe")
                .setDescription("Sammle Karten, öffne Packs und vervollständige deine Sammlung!")
                .setColor(0x3498DB);

        // Sammeln & Kaufen
        embed.addField(
                "📦 Sammeln & Kaufen",
                "`/daily` - Tägliche Belohnung (3 Karten + Coins)\n" +
                        "`/pack` - Kartenpack öffnen (100 Coins, 5 Karten)",
                false
        );

        // Sammlung verwalten
        embed.addField(
                "📚 Sammlung",
                "`/sammlung [user]` - Zeige Kartensammlung\n" +
                        "`/profil [user]` - Zeige komplettes Profil\n" +
                        "`/stats` - Zeige Statistiken\n" +
                        "`/karte <name>` - Zeige Kartendetails",
                false
        );

        // Handel & Wirtschaft
        embed.addField(
                "💰 Handel & Wirtschaft",
                "`/coins [user]` - Zeige Coin-Guthaben\n" +
                        "`/sell <karte>` - Verkaufe eine Karte\n" +
                        "`/trade <user> <give> <want>` - Tausche Karten\n" +
                        "`/gift <user> <karte>` - Verschenke eine Karte",
                false
        );

        // Suche & Organisation
        embed.addField(
                "🔍 Suche & Organisation",
                "`/search [name] [serie]` - Suche Karten\n" +
                        "`/wishlist add/remove/show` - Wunschliste verwalten\n" +
                        "`/leaderboard [kategorie]` - Top-Sammler anzeigen",
                false
        );

        embed.addField(
                "💡 Tipp",
                "Claime täglich deine Belohnung und spare Coins für Packs!",
                false
        );

        embed.setFooter("Nutze /help um diese Nachricht erneut zu sehen");

        event.replyEmbeds(embed.build()).queue();
    }
}