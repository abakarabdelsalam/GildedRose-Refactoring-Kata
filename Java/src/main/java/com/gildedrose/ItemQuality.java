package com.gildedrose;

/**
 * Bornes de la qualité d'un {@link Item} et opérations qui les respectent.
 * <p>
 * Ces deux méthodes appartiendraient naturellement à {@code Item}, mais le kata
 * interdit de modifier cette classe. Elles sont donc regroupées dans cette classe
 * compagne plutôt que dupliquées dans chaque implémentation de {@link ItemUpdater} :
 * les deux bornes n'existent qu'ici.
 */
final class ItemQuality {

    static final int MIN = 0;

    static final int MAX = 50;

    private ItemQuality() {
    }

    /**
     * Augmente la qualité d'un point sans jamais franchir {@link #MAX}.
     * Une qualité déjà supérieure à la borne reste intacte.
     */
    static void increase(Item item) {
        if (item.quality < MAX) {
            item.quality++;
        }
    }

    /**
     * Diminue la qualité d'un point sans jamais franchir {@link #MIN}.
     * Une qualité déjà inférieure à la borne reste intacte.
     */
    static void decrease(Item item) {
        if (item.quality > MIN) {
            item.quality--;
        }
    }
}
