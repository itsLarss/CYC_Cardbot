package dev.itsLarss;

import dev.itsLarss.commands.*;
import dev.itsLarss.database.DatabaseManager;
import dev.itsLarss.database.ServerSettingsManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class CardBot {

    private static JDA jda;
    private static DatabaseManager database;

    public static void main(String[] args) {
        String token = "MTQ2MjQ1MDg3MjQ5MDUyODgyMA.GGRWc8.pEJDbLuGrL-hun6r3LRnjArM9MokpVMxL-uH7A";

        try {
            // Initialisiere Datenbank
            database = new DatabaseManager();
            database.initialize();

            // Initialisiere Server Settings Manager (nach Database!)
            //serverSettings = new ServerSettingsManager(database.getConnection());

            // Erstelle JDA Instanz
            jda = JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MEMBERS
                    )
                    .build();

            // Warte bis Bot bereit ist
            jda.awaitReady();

            System.out.println("✅ Bot ist online: " + jda.getSelfUser().getAsTag());

            // Registriere Commands
            registerCommands();

        } catch (Exception e) {
            System.err.println("❌ Fehler beim Starten des Bots:");
            e.printStackTrace();
        }
    }

    private static void registerCommands() {
        // Slash Commands registrieren
        jda.updateCommands().addCommands(
                // Basis Commands
                new DailyCommand().getCommandData(),
                new PackCommand().getCommandData(),
                new SammlungCommand().getCommandData(),
                new KarteCommand().getCommandData(),
                new CoinsCommand().getCommandData(),
                new StatsCommand().getCommandData(),
                new HelpCommand().getCommandData(),

                // Trading & Economy
                new TradeCommand().getCommandData(),
                new SellCommand().getCommandData(),
                new GiftCommand().getCommandData(),

                // Utility
                new SearchCommand().getCommandData(),
                new ProfileCommand().getCommandData(),
                new WishlistCommand().getCommandData(),
                new LeaderboardCommand().getCommandData(),

                // Admin & Setup
                new SetupCommand().getCommandData(),
                new AdminCommand().getCommandData()
                // CleanupCommand ist optional - nur wenn du DatabaseManager_Fixed nutzt
        ).queue();

        // Event Listener registrieren
        jda.addEventListener(
                // Basis Commands
                new DailyCommand(),
                new PackCommand(),
                new SammlungCommand(),
                new KarteCommand(),
                new CoinsCommand(),
                new StatsCommand(),
                new HelpCommand(),

                // Trading & Economy
                new TradeCommand(),
                new SellCommand(),
                new GiftCommand(),

                // Utility
                new SearchCommand(),
                new ProfileCommand(),
                new WishlistCommand(),
                new LeaderboardCommand(),

                // Admin & Setup
                new SetupCommand(),
                new AdminCommand()
                // CleanupCommand ist optional
        );

        System.out.println("✅ Commands registriert!");
    }

    /**
     * Gibt die JDA Instanz zurück
     */
    public static JDA getJDA() {
        return jda;
    }

    /**
     * Gibt die Datenbank Instanz zurück
     */
    public static DatabaseManager getDatabase() {
        return database;
    }

    /**
     * Gibt den Server Settings Manager zurück
     */
    public static ServerSettingsManager getServerSettings() {
        return serverSettings;
    }
}