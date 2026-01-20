package dev.itsLarss.util;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class NSFWManager {

    /**
     * Prüft ob der Channel NSFW ist
     *
     * @param event Das Command-Event
     * @return true wenn NSFW-Channel, false sonst
     */
    public static boolean isNSFWChannel(SlashCommandInteractionEvent event) {
        // DMs sind NICHT NSFW
        if (event.getChannelType() == ChannelType.PRIVATE) {
            return false;
        }

        // Prüfe ob Guild-Channel
        if (event.getChannel() instanceof GuildMessageChannel) {
            GuildMessageChannel channel = (GuildMessageChannel) event.getChannel();

            // TextChannel hat nsfw-Flag
            if (channel instanceof TextChannel) {
                TextChannel textChannel = (TextChannel) channel;
                return textChannel.isNSFW();
            }
        }

        return false;
    }

    /**
     * Blockt NSFW-Content in SFW-Channels
     *
     * @param event Das Command-Event
     * @param commandName Name des Commands (für Error-Message)
     * @return true wenn geblockt wurde, false wenn OK
     */
    public static boolean blockIfNotNSFW(SlashCommandInteractionEvent event, String commandName) {
        if (!isNSFWChannel(event)) {
            event.reply("🔞 **NSFW-Content nur in NSFW-Channels!**\n\n" +
                            "Dieser Command kann nur in Channels verwendet werden, die als **NSFW markiert** sind.\n\n" +
                            "**So aktivierst du NSFW:**\n" +
                            "1. Rechtsklick auf Channel\n" +
                            "2. Channel bearbeiten\n" +
                            "3. \"Age-Restricted Channel\" aktivieren\n\n" +
                            "⚠️ **Wichtig:** NSFW-Content ist nur für Nutzer über 18 Jahren!")
                    .setEphemeral(true)
                    .queue();
            return true;
        }
        return false;
    }

    /**
     * Zeigt Age-Gate Warnung (optional, für extra Sicherheit)
     *
     * @param hook Der Interaction Hook
     */
    public static void showAgeGateWarning(InteractionHook hook) {
        hook.sendMessage(
                "⚠️ **ALTERSBESCHRÄNKUNG - 18+**\n\n" +
                        "Der NSFW-Modus enthält Inhalte die:\n" +
                        "• Nicht für Minderjährige geeignet sind\n" +
                        "• Explizite oder suggestive Darstellungen enthalten können\n" +
                        "• Nur in NSFW-Channels verfügbar sind\n\n" +
                        "Durch die Nutzung bestätigst du, dass du **über 18 Jahre alt** bist."
        ).setEphemeral(true).queue();
    }

    /**
     * Gibt Info-Text für NSFW-Status zurück
     */
    public static String getNSFWStatusText(SlashCommandInteractionEvent event) {
        if (isNSFWChannel(event)) {
            return "🔞 NSFW-Modus aktiv";
        } else {
            return "✅ SFW-Modus (sicher für alle Altersgruppen)";
        }
    }
}