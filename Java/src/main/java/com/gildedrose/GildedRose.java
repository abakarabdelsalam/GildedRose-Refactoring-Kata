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
        for (int i = 0; i < items.length; i++) {
            if (!items[i].name.equals(AGED_BRIE)
                    && !items[i].name.equals(BACKSTAGE_PASS)) {
                if (items[i].quality > MIN_QUALITY) {
                    if (!items[i].name.equals(SULFURAS)) {
                        items[i].quality = items[i].quality - 1;
                    }
                }
            } else {
                if (items[i].quality < MAX_QUALITY) {
                    items[i].quality = items[i].quality + 1;

                    if (items[i].name.equals(BACKSTAGE_PASS)) {
                        if (items[i].sellIn <= BACKSTAGE_DOUBLE_GAIN_SELL_IN) {
                            if (items[i].quality < MAX_QUALITY) {
                                items[i].quality = items[i].quality + 1;
                            }
                        }

                        if (items[i].sellIn <= BACKSTAGE_TRIPLE_GAIN_SELL_IN) {
                            if (items[i].quality < MAX_QUALITY) {
                                items[i].quality = items[i].quality + 1;
                            }
                        }
                    }
                }
            }

            if (!items[i].name.equals(SULFURAS)) {
                items[i].sellIn = items[i].sellIn - 1;
            }

            if (items[i].sellIn < 0) {
                if (!items[i].name.equals(AGED_BRIE)) {
                    if (!items[i].name.equals(BACKSTAGE_PASS)) {
                        if (items[i].quality > MIN_QUALITY) {
                            if (!items[i].name.equals(SULFURAS)) {
                                items[i].quality = items[i].quality - 1;
                            }
                        }
                    } else {
                        items[i].quality = MIN_QUALITY;
                    }
                } else {
                    if (items[i].quality < MAX_QUALITY) {
                        items[i].quality = items[i].quality + 1;
                    }
                }
            }
        }
    }
}
