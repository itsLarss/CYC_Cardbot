package dev.itsLarss.model;

public class Card {

    private final int id;
    private final String name;
    private final CardRarity rarity;
    private final String description;
    private final String series;
    private final String imageUrl;

    // Konstruktor MIT Bild
    public Card(int id, String name, CardRarity rarity, String description, String series, String imageUrl) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.description = description;
        this.series = series;
        this.imageUrl = imageUrl;
    }

    // Konstruktor OHNE Bild (für alte Kompatibilität)
    public Card(int id, String name, CardRarity rarity, String description, String series) {
        this(id, name, rarity, description, series, null);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CardRarity getRarity() {
        return rarity;
    }

    public String getDescription() {
        return description;
    }

    public String getSeries() {
        return series;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    /**
     * Prüft ob die Karte NSFW ist basierend auf der URL
     * WICHTIG: NSFW Karten MÜSSEN "NSFW_" im Pfad haben!
     */
    public boolean isNSFW() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false; // Karten ohne Bild sind SFW
        }

        // Prüfe ob "NSFW_" oder "/NSFW_" oder "/nsfw_" im Pfad ist
        String lowerUrl = imageUrl.toLowerCase();
        return lowerUrl.contains("/nsfw_") || lowerUrl.contains("nsfw_charakter");
    }

    /**
     * Prüft ob die Karte SFW (Safe For Work) ist
     */
    public boolean isSFW() {
        return !isNSFW();
    }

    @Override
    public String toString() {
        return name + " (" + rarity.getName() + ")";
    }
}