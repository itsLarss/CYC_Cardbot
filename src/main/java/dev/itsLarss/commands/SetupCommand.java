package dev.itsLarss.commands;

import dev.itsLarss.CardBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

/**
 * Setup Command - Nur für Server Owner!
 * Konfiguriert:
 * - Erlaubte Channels
 * - NSFW Mode (nur in NSFW-Channels)
 */
public class SetupCommand extends ListenerAdapter {

    public CommandData getCommandData() {
        return Commands.slash("setup", "⚙️ Bot-Einstellungen (nur Server Owner)")
                .addSubcommands(
                        new SubcommandData("channel", "Setze erlaubte Channels für den Bot")
                                .addOption(OptionType.CHANNEL, "channel", "Channel auswählen", true)
                                .addOption(OptionType.STRING, "action", "add oder remove", true),
                        new SubcommandData("nsfw", "NSFW Mode aktivieren/deaktivieren")
                                .addOption(OptionType.BOOLEAN, "enabled", "true = aktiviert, false = deaktiviert", true),
                        new SubcommandData("view", "Zeige aktuelle Einstellungen"),
                        new SubcommandData("reset", "Setze alle Einstellungen zurück")
                );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("setup")) return;

        // Nur in Servern (nicht in DMs)
        if (!event.isFromGuild()) {
            event.reply("❌ Dieser Command funktioniert nur auf Servern!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Nur Server Owner darf Setup nutzen!
        Member member = event.getMember();
        if (member == null || !member.isOwner()) {
            event.reply("❌ Nur der **Server Owner** kann Setup-Befehle nutzen!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String subcommand = event.getSubcommandName();
        String guildId = event.getGuild().getId();

        switch (subcommand) {
            case "channel":
                handleChannelSetup(event, guildId);
                break;
            case "nsfw":
                handleNSFWSetup(event, guildId);
                break;
            case "view":
                handleViewSettings(event, guildId);
                break;
            case "reset":
                handleReset(event, guildId);
                break;
        }
    }

    /**
     * Channel Setup - Füge/Entferne erlaubte Channels
     */
    private void handleChannelSetup(SlashCommandInteractionEvent event, String guildId) {
        var channelOption = event.getOption("channel");
        String action = event.getOption("action").getAsString().toLowerCase();

        if (channelOption == null) {
            event.reply("❌ Bitte wähle einen Channel!").setEphemeral(true).queue();
            return;
        }

        var channel = channelOption.getAsChannel();

        // Nur Text-Channels erlauben
        if (channel.getType() != ChannelType.TEXT) {
            event.reply("❌ Nur Text-Channels sind erlaubt!").setEphemeral(true).queue();
            return;
        }

        String channelId = channel.getId();

        if (action.equals("add")) {
            CardBot.getServerSettings().addAllowedChannel(guildId, channelId);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("✅ Channel hinzugefügt!")
                    .setDescription("Der Bot kann jetzt in <#" + channelId + "> genutzt werden.")
                    .setColor(0x2ECC71);

            event.replyEmbeds(embed.build()).queue();

        } else if (action.equals("remove")) {
            CardBot.getServerSettings().removeAllowedChannel(guildId, channelId);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("✅ Channel entfernt!")
                    .setDescription("<#" + channelId + "> wurde aus der Liste entfernt.")
                    .setColor(0xE74C3C);

            event.replyEmbeds(embed.build()).queue();

        } else {
            event.reply("❌ Aktion muss 'add' oder 'remove' sein!").setEphemeral(true).queue();
        }
    }

    /**
     * NSFW Setup - Aktiviere/Deaktiviere NSFW Mode
     */
    private void handleNSFWSetup(SlashCommandInteractionEvent event, String guildId) {
        boolean enabled = event.getOption("enabled").getAsBoolean();

        if (enabled) {
            CardBot.getServerSettings().enableNSFW(guildId);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🔞 NSFW Mode aktiviert!")
                    .setDescription(
                            "**WICHTIG:** NSFW-Karten können jetzt angezeigt werden!\n\n" +
                                    "⚠️ **Discord-Regeln:**\n" +
                                    "• NSFW-Karten werden **NUR** in NSFW-markierten Channels angezeigt\n" +
                                    "• In normalen Channels werden nur SFW-Karten gezeigt\n" +
                                    "• Du musst einen Channel als NSFW markieren (Channel-Einstellungen)\n\n" +
                                    "🔒 **Sicherheit:**\n" +
                                    "• Minderjährige können keine NSFW-Inhalte sehen\n"
                    )
                    .setColor(0xFF0000)
                    .setFooter("Stelle sicher dass dein Server Discord's NSFW-Regeln befolgt!");

            event.replyEmbeds(embed.build()).queue();

        } else {
            CardBot.getServerSettings().disableNSFW(guildId);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("✅ NSFW Mode deaktiviert!")
                    .setDescription("Es werden nur noch SFW-Karten (Safe For Work) angezeigt.")
                    .setColor(0x2ECC71);

            event.replyEmbeds(embed.build()).queue();
        }
    }

    /**
     * Zeige aktuelle Einstellungen
     */
    private void handleViewSettings(SlashCommandInteractionEvent event, String guildId) {
        var settings = CardBot.getServerSettings();

        boolean nsfwEnabled = settings.isNSFWEnabled(guildId);
        var allowedChannels = settings.getAllowedChannels(guildId);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⚙️ " + event.getGuild().getName() + " - Einstellungen")
                .setColor(0x3498DB);

        // NSFW Status
        embed.addField(
                "🔞 NSFW Mode",
                nsfwEnabled ? "✅ Aktiviert (NSFW-Karten in NSFW-Channels)" : "❌ Deaktiviert (nur SFW)",
                false
        );

        // Erlaubte Channels
        if (allowedChannels.isEmpty()) {
            embed.addField(
                    "📝 Erlaubte Channels",
                    "**Alle Channels** (keine Einschränkung)",
                    false
            );
        } else {
            StringBuilder channelList = new StringBuilder();
            for (String channelId : allowedChannels) {
                channelList.append("• <#").append(channelId).append(">\n");
            }
            embed.addField(
                    "📝 Erlaubte Channels",
                    channelList.toString(),
                    false
            );
        }

        embed.setFooter("Nutze /setup um Einstellungen zu ändern");

        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Reset alle Einstellungen
     */
    private void handleReset(SlashCommandInteractionEvent event, String guildId) {
        CardBot.getServerSettings().resetServer(guildId);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔄 Einstellungen zurückgesetzt!")
                .setDescription(
                        "Alle Einstellungen wurden gelöscht:\n\n" +
                                "• NSFW Mode: **Deaktiviert**\n" +
                                "• Erlaubte Channels: **Alle**\n\n" +
                                "Nutze `/setup` um neu zu konfigurieren."
                )
                .setColor(0x95A5A6);

        event.replyEmbeds(embed.build()).queue();
    }
}