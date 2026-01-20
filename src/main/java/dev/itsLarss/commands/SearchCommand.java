package dev.itsLarss.commands;

import dev.itsLarss.model.Card;
import dev.itsLarss.model.CardRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search Command - Suche nach Karten
 */
public class SearchCommand extends ListenerAdapter {

    private static final int CARDS_PER_PAGE = 15;

    // Speichert aktive Suchen: MessageID -> SearchSession
    private static Map<String, SearchSession> activeSessions = new HashMap<>();

    public CommandData getCommandData() {
        return Commands.slash("search", "Suche nach Karten")
                .addOption(OptionType.STRING, "name", "Kartenname (teilweise)", false)
                .addOption(OptionType.STRING, "serie", "Serie", false);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("search")) return;

        String searchName = event.getOption("name") != null
                ? event.getOption("name").getAsString().toLowerCase()
                : null;

        String searchSeries = event.getOption("serie") != null
                ? event.getOption("serie").getAsString().toLowerCase()
                : null;

        if (searchName == null && searchSeries == null) {
            event.reply("❌ Bitte gib mindestens einen Suchbegriff an!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Suche Karten
        List<Card> results = new ArrayList<>();

        for (Card card : CardRegistry.getAllCards()) {
            boolean matches = true;

            if (searchName != null && !card.getName().toLowerCase().contains(searchName)) {
                matches = false;
            }

            if (searchSeries != null && !card.getSeries().toLowerCase().contains(searchSeries)) {
                matches = false;
            }

            if (matches) {
                results.add(card);
            }
        }

        if (results.isEmpty()) {
            event.reply("📭 Keine Karten gefunden!")
                    .queue();
            return;
        }

        // Sortiere nach Seltenheit (selten zuerst)
        results.sort((a, b) -> {
            int rarityCompare = Integer.compare(
                    b.getRarity().ordinal(),
                    a.getRarity().ordinal()
            );
            if (rarityCompare != 0) return rarityCompare;
            return a.getName().compareTo(b.getName());
        });

        // Erstelle Session
        SearchSession session = new SearchSession(results, searchName, searchSeries);

        // Sende erste Seite
        EmbedBuilder embed = createPageEmbed(session, 0);
        List<Button> buttons = createNavigationButtons(0, session.getTotalPages());

        if (buttons.isEmpty()) {
            // Nur eine Seite - keine Buttons nötig
            event.replyEmbeds(embed.build()).queue();
        } else {
            event.replyEmbeds(embed.build())
                    .addActionRow(buttons)
                    .queue(message -> {
                        message.retrieveOriginal().queue(msg -> {
                            activeSessions.put(msg.getId(), session);

                            // Cleanup nach 5 Minuten
                            new Thread(() -> {
                                try {
                                    Thread.sleep(300000); // 5 Min
                                    activeSessions.remove(msg.getId());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        });
                    });
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();

        if (!buttonId.startsWith("search_page_")) return;

        // Ignoriere disabled Buttons
        if (buttonId.endsWith("_disabled") || buttonId.equals("search_page_current")) {
            return;
        }

        String messageId = event.getMessageId();
        SearchSession session = activeSessions.get(messageId);

        if (session == null) {
            event.reply("❌ Diese Suche ist abgelaufen! Nutze `/search` erneut.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Bestimme Ziel-Seite basierend auf Button
        int currentPage = session.currentPage;
        int targetPage = currentPage;
        int totalPages = session.getTotalPages();

        switch (buttonId) {
            case "search_page_first":
                targetPage = 0;
                break;
            case "search_page_prev":
                targetPage = Math.max(0, currentPage - 1);
                break;
            case "search_page_next":
                targetPage = Math.min(totalPages - 1, currentPage + 1);
                break;
            case "search_page_last":
                targetPage = totalPages - 1;
                break;
        }

        // Update Session
        session.currentPage = targetPage;

        // Erstelle neue Seite
        EmbedBuilder embed = createPageEmbed(session, targetPage);
        List<Button> buttons = createNavigationButtons(targetPage, session.getTotalPages());

        // Update Nachricht
        event.editMessageEmbeds(embed.build())
                .setActionRow(buttons)
                .queue();
    }

    /**
     * Erstellt das Embed für eine bestimmte Seite
     */
    private EmbedBuilder createPageEmbed(SearchSession session, int page) {
        int startIndex = page * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, session.results.size());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔍 Suchergebnisse")
                .setColor(0x3498DB);

        // Beschreibung mit Suchkriterien
        StringBuilder description = new StringBuilder();
        description.append("**Gefunden:** ").append(session.results.size()).append(" Karten\n");
        if (session.searchName != null) {
            description.append("**Name enthält:** ").append(session.searchName).append("\n");
        }
        if (session.searchSeries != null) {
            description.append("**Serie enthält:** ").append(session.searchSeries).append("\n");
        }
        description.append("\n**Seite ").append(page + 1).append("/").append(session.getTotalPages()).append("**");

        embed.setDescription(description.toString());

        // Zeige Karten für diese Seite
        List<Card> pageCards = session.results.subList(startIndex, endIndex);

        for (int i = 0; i < pageCards.size(); i++) {
            Card card = pageCards.get(i);
            int globalIndex = startIndex + i + 1;

            String fieldName = String.format("%d. %s %s",
                    globalIndex,
                    card.getRarity().getEmoji(),
                    card.getName()
            );

            String fieldValue = String.format("**%s** - %s\nSerie: %s",
                    card.getRarity().getName(),
                    card.getDescription(),
                    card.getSeries()
            );

            embed.addField(fieldName, fieldValue, false);
        }

        embed.setFooter("Nutze /karte <n> um Details zu sehen");

        return embed;
    }

    /**
     * Erstellt die Navigation-Buttons
     */
    private List<Button> createNavigationButtons(int currentPage, int totalPages) {
        List<Button> buttons = new ArrayList<>();

        // Nur Buttons wenn mehr als 1 Seite
        if (totalPages <= 1) {
            return buttons;
        }

        // Erste Seite Button
        if (currentPage > 0) {
            buttons.add(Button.primary("search_page_first", "⏮️ Erste"));
        } else {
            buttons.add(Button.primary("search_page_first_disabled", "⏮️ Erste").asDisabled());
        }

        // Vorherige Seite Button
        if (currentPage > 0) {
            buttons.add(Button.primary("search_page_prev", "◀️ Zurück"));
        } else {
            buttons.add(Button.primary("search_page_prev_disabled", "◀️ Zurück").asDisabled());
        }

        // Seiten-Info (nicht klickbar)
        buttons.add(Button.secondary("search_page_current",
                (currentPage + 1) + "/" + totalPages).asDisabled());

        // Nächste Seite Button
        if (currentPage < totalPages - 1) {
            buttons.add(Button.primary("search_page_next", "▶️ Weiter"));
        } else {
            buttons.add(Button.primary("search_page_next_disabled", "▶️ Weiter").asDisabled());
        }

        // Letzte Seite Button
        if (currentPage < totalPages - 1) {
            buttons.add(Button.primary("search_page_last", "⏭️ Letzte"));
        } else {
            buttons.add(Button.primary("search_page_last_disabled", "⏭️ Letzte").asDisabled());
        }

        return buttons;
    }

    /**
     * Hilfsklasse für Search-Sessions
     */
    private static class SearchSession {
        List<Card> results;
        String searchName;
        String searchSeries;
        int currentPage;

        SearchSession(List<Card> results, String searchName, String searchSeries) {
            this.results = results;
            this.searchName = searchName;
            this.searchSeries = searchSeries;
            this.currentPage = 0;
        }

        int getTotalPages() {
            return (int) Math.ceil((double) results.size() / CARDS_PER_PAGE);
        }
    }
}