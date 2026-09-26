package com.gildedrose;

/**
 * Billet de concert : la qualité gagne un point par jour, deux à dix jours du concert
 * ou moins, trois à cinq jours ou moins, et tombe à zéro une fois le concert passé.
 */
class BackstagePassUpdater implements ItemUpdater {

    private static final int DOUBLE_GAIN_SELL_IN = 10;

    private static final int TRIPLE_GAIN_SELL_IN = 5;

    @Override
    public void update(Item item) {
        ItemQuality.increase(item);
        if (item.sellIn <= DOUBLE_GAIN_SELL_IN) {
            ItemQuality.increase(item);
        }
        if (item.sellIn <= TRIPLE_GAIN_SELL_IN) {
            ItemQuality.increase(item);
        }
        item.sellIn--;
        if (isExpired(item)) {
            item.quality = ItemQuality.MIN;
        }
    }
}
