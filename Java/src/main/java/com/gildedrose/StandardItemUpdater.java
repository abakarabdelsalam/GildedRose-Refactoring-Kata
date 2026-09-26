package com.gildedrose;

/** Article ordinaire : la qualité baisse d'un point par jour, de deux une fois périmé. */
class StandardItemUpdater implements ItemUpdater {

    @Override
    public void update(Item item) {
        ItemQuality.decrease(item);
        item.sellIn--;
        if (isExpired(item)) {
            ItemQuality.decrease(item);
        }
    }
}
