package com.gildedrose;

/** Article ordinaire : la qualité baisse d'un point par jour, de deux une fois périmé. */
class StandardItemUpdater implements ItemUpdater {

    @Override
    public void update(Item item) {
        Items.decreaseQuality(item);
        item.sellIn--;
        if (Items.isExpired(item)) {
            Items.decreaseQuality(item);
        }
    }
}
