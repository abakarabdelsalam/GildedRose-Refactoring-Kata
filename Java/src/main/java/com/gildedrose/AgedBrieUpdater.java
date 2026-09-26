package com.gildedrose;

/** Le Brie vieilli gagne un point par jour, deux une fois la date de vente dépassée. */
class AgedBrieUpdater implements ItemUpdater {

    @Override
    public void update(Item item) {
        ItemQuality.increase(item);
        item.sellIn--;
        if (isExpired(item)) {
            ItemQuality.increase(item);
        }
    }
}
