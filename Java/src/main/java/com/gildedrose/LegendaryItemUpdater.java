package com.gildedrose;

/** Article légendaire : il ne se périme jamais et ne perd jamais de qualité. */
class LegendaryItemUpdater implements ItemUpdater {

    @Override
    public void update(Item item) {
        // Volontairement vide : ni la qualité ni le délai de vente ne changent.
    }
}
