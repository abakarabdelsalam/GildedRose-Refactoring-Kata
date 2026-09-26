package com.gildedrose;

class GildedRose {

    private static final String AGED_BRIE = "Aged Brie";

    private static final String BACKSTAGE_PASS = "Backstage passes to a TAFKAL80ETC concert";

    private static final String SULFURAS = "Sulfuras, Hand of Ragnaros";

    private static final int MIN_QUALITY = 0;

    private static final int MAX_QUALITY = 50;

    /** À ce nombre de jours restants ou moins, un backstage pass gagne 2 points par jour. */
    private static final int BACKSTAGE_DOUBLE_GAIN_SELL_IN = 10;

    /** À ce nombre de jours restants ou moins, un backstage pass gagne 3 points par jour. */
    private static final int BACKSTAGE_TRIPLE_GAIN_SELL_IN = 5;

    Item[] items;

    public GildedRose(Item[] items) {
        this.items = items;
    }

    public void updateQuality() {
        for (Item item : items) {
            updateItem(item);
        }
    }

    private static void updateItem(Item item) {
        if (isLegendary(item)) {
            return;
        }
        applyDailyQualityChange(item);
        item.sellIn--;
        // Le décrément précède le test de péremption : un article dont il reste 0 jour
        // subit donc déjà le traitement des articles périmés le jour même.
        if (isExpired(item)) {
            applyExpiredQualityChange(item);
        }
    }

    private static void applyDailyQualityChange(Item item) {
        if (isAgedBrie(item)) {
            increaseQuality(item);
        } else if (isBackstagePass(item)) {
            increaseQuality(item);
            if (item.sellIn <= BACKSTAGE_DOUBLE_GAIN_SELL_IN) {
                increaseQuality(item);
            }
            if (item.sellIn <= BACKSTAGE_TRIPLE_GAIN_SELL_IN) {
                increaseQuality(item);
            }
        } else {
            decreaseQuality(item);
        }
    }

    private static void applyExpiredQualityChange(Item item) {
        if (isAgedBrie(item)) {
            increaseQuality(item);
        } else if (isBackstagePass(item)) {
            item.quality = MIN_QUALITY;
        } else {
            decreaseQuality(item);
        }
    }

    private static void increaseQuality(Item item) {
        if (item.quality < MAX_QUALITY) {
            item.quality++;
        }
    }

    private static void decreaseQuality(Item item) {
        if (item.quality > MIN_QUALITY) {
            item.quality--;
        }
    }

    private static boolean isLegendary(Item item) {
        return SULFURAS.equals(item.name);
    }

    private static boolean isAgedBrie(Item item) {
        return AGED_BRIE.equals(item.name);
    }

    private static boolean isBackstagePass(Item item) {
        return BACKSTAGE_PASS.equals(item.name);
    }

    private static boolean isExpired(Item item) {
        return item.sellIn < 0;
    }
}
