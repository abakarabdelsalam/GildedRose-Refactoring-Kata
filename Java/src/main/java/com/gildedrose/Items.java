package com.gildedrose;

/**
 * Opérations élémentaires sur un {@link Item} et bornes de sa qualité.
 * <p>
 * Ces méthodes appartiendraient naturellement à {@code Item}, mais le kata interdit
 * de modifier cette classe. Elles sont donc regroupées dans cette classe compagne
 * plutôt que dupliquées dans chaque implémentation de {@link ItemUpdater} : les deux
 * bornes de qualité n'existent qu'ici.
 */
final class Items {

    static final int MIN_QUALITY = 0;

    static final int MAX_QUALITY = 50;

    private Items() {
    }

    /**
     * Augmente la qualité d'un point sans jamais franchir {@link #MAX_QUALITY}.
     * Une qualité déjà supérieure à la borne reste intacte.
     */
    static void increaseQuality(Item item) {
        if (item.quality < MAX_QUALITY) {
            item.quality++;
        }
    }

    /**
     * Diminue la qualité d'un point sans jamais franchir {@link #MIN_QUALITY}.
     * Une qualité déjà inférieure à la borne reste intacte.
     */
    static void decreaseQuality(Item item) {
        if (item.quality > MIN_QUALITY) {
            item.quality--;
        }
    }

    static boolean isExpired(Item item) {
        return item.sellIn < 0;
    }
}
