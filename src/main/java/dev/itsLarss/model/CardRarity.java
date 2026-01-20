package dev.itsLarss.model;

import java.awt.Color;

public enum CardRarity {
    // SFW Rarities
    COMMON("Common", "⚪", 0x95A5A6, 10, false),
    UNCOMMON("Uncommon", "🟢", 0x2ECC71, 25, false),
    RARE("Rare", "🔵", 0x3498DB, 50, false),
    EPIC("Epic", "🟣", 0x9B59B6, 100, false),
    LEGENDARY("Legendary", "🟠", 0xE67E22, 250, false),
    MYTHIC("Mythic", "🟡", 0xF1C40F, 500, false),

    // NSFW Rarities (separate!)
    NSFW_COMMON("NSFW Common", "🔞⚪", 0xFF1493, 35, true),
    NSFW_UNCOMMON("NSFW Uncommon", "🔞🟢", 0xFF1493, 50, true),
    NSFW_RARE("NSFW Rare", "🔞🔵", 0xFF1493, 75, true),
    NSFW_EPIC("NSFW Epic", "🔞🟣", 0xFF69B4, 150, true),
    NSFW_LEGENDARY("NSFW Legendary", "🔞🟠", 0xFF1493, 300, true),
    NSFW_MYTHIC("NSFW Mythic", "🔞🟡", 0xFF0080, 600, true);

    private final String name;
    private final String emoji;
    private final int color;
    private final int sellValue;
    private final boolean nsfw;

    CardRarity(String name, String emoji, int color, int sellValue, boolean nsfw) {
        this.name = name;
        this.emoji = emoji;
        this.color = color;
        this.sellValue = sellValue;
        this.nsfw = nsfw;
    }

    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getColor() {
        return color;
    }

    public int getSellValue() {
        return sellValue;
    }

    public boolean isNSFW() {
        return nsfw;
    }

    /**
     * Prüft ob diese Seltenheit SFW ist
     */
    public boolean isSFW() {
        return !nsfw;
    }
}