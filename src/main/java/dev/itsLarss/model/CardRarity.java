package dev.itsLarss.model;

import java.awt.Color;

public enum CardRarity {

    COMMON("Common", 50.0, Color.decode("#9E9E9E"), "⚪"), //NSFW & SFW Charakter cards
    UNCOMMON("Uncommon", 25.0, Color.decode("#4CAF50"), "🟢"), //NSFW & SFW Charakter cards
    RARE("Rare", 15.0, Color.decode("#2196F3"), "🔵"), // nothing yet
    EPIC("Epic", 7.0, Color.decode("#9C27B0"), "🟣"), // nothing yet
    LEGENDARY("Legendary", 2.5, Color.decode("#FF9800"), "🟠"), //Special Cards
    MYTHIC("Mythic", 0.5, Color.decode("#FFD700"), "🟡"); //Super Special Cards

    private final String name;
    private final double dropChance;
    private final Color color;
    private final String emoji;

    CardRarity(String name, double dropChance, Color color, String emoji) {
        this.name = name;
        this.dropChance = dropChance;
        this.color = color;
        this.emoji = emoji;
    }

    public String getName() {
        return name;
    }

    public double getDropChance() {
        return dropChance;
    }

    public Color getColor() {
        return color;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getSellPrice() {
        switch (this) {
            case COMMON: return 5;
            case UNCOMMON: return 15;
            case RARE: return 40;
            case EPIC: return 100;
            case LEGENDARY: return 300;
            case MYTHIC: return 1000;
            default: return 5;
        }
    }

    /**
     * Gibt die Gesamtwahrscheinlichkeit aller Seltenheiten zurück
     */
    public static double getTotalChance() {
        double total = 0;
        for (CardRarity rarity : values()) {
            total += rarity.dropChance;
        }
        return total;
    }
}