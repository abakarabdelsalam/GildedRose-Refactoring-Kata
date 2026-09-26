package com.gildedrose;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Choisit la règle de mise à jour correspondant à un article, d'après son nom.
 * <p>
 * Les articles sans règle propre relèvent de {@link StandardItemUpdater}. Les règles
 * sont sans état, donc partagées entre tous les articles.
 */
final class ItemUpdaters {

    private static final String AGED_BRIE = "Aged Brie";

    private static final String BACKSTAGE_PASS = "Backstage passes to a TAFKAL80ETC concert";

    private static final String SULFURAS = "Sulfuras, Hand of Ragnaros";

    private static final ItemUpdater DEFAULT_UPDATER = new StandardItemUpdater();

    private static final Map<String, ItemUpdater> UPDATERS_BY_NAME = updatersByName();

    private ItemUpdaters() {
    }

    static ItemUpdater forItem(Item item) {
        ItemUpdater updater = UPDATERS_BY_NAME.get(item.name);
        return updater != null ? updater : DEFAULT_UPDATER;
    }

    private static Map<String, ItemUpdater> updatersByName() {
        Map<String, ItemUpdater> updaters = new HashMap<>();
        updaters.put(AGED_BRIE, new AgedBrieUpdater());
        updaters.put(BACKSTAGE_PASS, new BackstagePassUpdater());
        updaters.put(SULFURAS, new LegendaryItemUpdater());
        return Collections.unmodifiableMap(updaters);
    }
}
