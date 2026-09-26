package com.gildedrose;

/** Article invoqué : sa qualité se dégrade deux fois plus vite que celle d'un article ordinaire. */
class ConjuredItemUpdater implements ItemUpdater {

    private static final int DEGRADATION_RATE = 2;

    @Override
    public void update(Item item) {
        degradeQuality(item);
        item.sellIn--;
        if (Items.isExpired(item)) {
            degradeQuality(item);
        }
    }

    private static void degradeQuality(Item item) {
        for (int step = 0; step < DEGRADATION_RATE; step++) {
            Items.decreaseQuality(item);
        }
    }
}
