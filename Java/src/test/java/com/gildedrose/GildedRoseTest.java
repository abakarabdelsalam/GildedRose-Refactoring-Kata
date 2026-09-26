package com.gildedrose;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests de caractérisation par règle métier.
 * <p>
 * Les noms d'articles sont redéclarés ici en littéraux, volontairement : les tests
 * doivent décrire la spécification indépendamment du code de production, afin de
 * détecter une faute de frappe introduite dans une constante du code métier.
 */
@DisplayName("Mise à jour quotidienne de l'inventaire")
class GildedRoseTest {

    private static final String NORMAL_ITEM = "+5 Dexterity Vest";

    private static final String AGED_BRIE = "Aged Brie";

    private static final String SULFURAS = "Sulfuras, Hand of Ragnaros";

    private static final String BACKSTAGE_PASS = "Backstage passes to a TAFKAL80ETC concert";

    private static final String CONJURED_ITEM = "Conjured Mana Cake";

    @Nested
    @DisplayName("Article ordinaire")
    class NormalItem {

        @Test
        @DisplayName("perd 1 jour et 1 point de qualité avant la date de péremption")
        void losesOneQualityPerDayBeforeSellByDate() {
            assertItem(afterOneDay(NORMAL_ITEM, 10, 20), 9, 19);
        }

        @Test
        @DisplayName("perd encore seulement 1 point de qualité le dernier jour de vente")
        void losesOneQualityOnTheLastSellableDay() {
            assertItem(afterOneDay(NORMAL_ITEM, 1, 20), 0, 19);
        }

        @Test
        @DisplayName("perd 2 points de qualité le jour où la date de péremption est dépassée")
        void losesTwoQualityOnTheDayTheSellByDatePasses() {
            assertItem(afterOneDay(NORMAL_ITEM, 0, 20), -1, 18);
        }

        @Test
        @DisplayName("perd 2 points de qualité par jour une fois périmé")
        void losesTwoQualityPerDayAfterSellByDate() {
            assertItem(afterOneDay(NORMAL_ITEM, -1, 20), -2, 18);
        }

        @Test
        @DisplayName("ne descend jamais en dessous de 0")
        void qualityNeverGoesBelowZero() {
            assertItem(afterOneDay(NORMAL_ITEM, 5, 0), 4, 0);
        }

        @Test
        @DisplayName("s'arrête à 0 au lieu de devenir négatif quand il est périmé")
        void qualityStopsAtZeroInsteadOfGoingNegativeWhenExpired() {
            assertItem(afterOneDay(NORMAL_ITEM, 0, 1), -1, 0);
        }
    }

    @Nested
    @DisplayName("Aged Brie")
    class AgedBrie {

        @Test
        @DisplayName("gagne 1 point de qualité par jour avant la date de péremption")
        void gainsOneQualityPerDayBeforeSellByDate() {
            assertItem(afterOneDay(AGED_BRIE, 2, 0), 1, 1);
        }

        @Test
        @DisplayName("gagne 2 points de qualité par jour une fois périmé")
        void gainsTwoQualityPerDayAfterSellByDate() {
            assertItem(afterOneDay(AGED_BRIE, 0, 10), -1, 12);
        }

        @Test
        @DisplayName("ne dépasse jamais 50")
        void qualityNeverExceedsFifty() {
            assertItem(afterOneDay(AGED_BRIE, 5, 50), 4, 50);
        }

        @Test
        @DisplayName("plafonne à 50 au lieu de dépasser quand il gagne 2 points depuis 49")
        void qualityCapsAtFiftyInsteadOfOvershootingFromFortyNine() {
            assertItem(afterOneDay(AGED_BRIE, -1, 49), -2, 50);
        }
    }

    @Nested
    @DisplayName("Sulfuras, objet légendaire")
    class Sulfuras {

        @Test
        @DisplayName("ne perd ni qualité ni jour de vente")
        void neverLosesQualityNorSellIn() {
            assertItem(afterOneDay(SULFURAS, 0, 80), 0, 80);
        }

        @Test
        @DisplayName("reste inchangé même avec un sellIn négatif")
        void staysUnchangedEvenWithNegativeSellIn() {
            assertItem(afterOneDay(SULFURAS, -1, 80), -1, 80);
        }

        @Test
        @DisplayName("garde une qualité de 80, au-dessus du plafond habituel de 50")
        void keepsQualityOfEightyAboveTheUsualCap() {
            assertItem(afterOneDay(SULFURAS, 10, 80), 10, 80);
        }
    }

    @Nested
    @DisplayName("Backstage passes")
    class BackstagePasses {

        @Test
        @DisplayName("gagne 1 point quand il reste plus de 10 jours")
        void gainsOneQualityWhenMoreThanTenDaysRemain() {
            assertItem(afterOneDay(BACKSTAGE_PASS, 11, 20), 10, 21);
        }

        @Test
        @DisplayName("gagne 2 points quand il reste 10 jours")
        void gainsTwoQualityWhenTenDaysRemain() {
            assertItem(afterOneDay(BACKSTAGE_PASS, 10, 20), 9, 22);
        }

        @Test
        @DisplayName("gagne 2 points quand il reste 6 jours")
        void gainsTwoQualityWhenSixDaysRemain() {
            assertItem(afterOneDay(BACKSTAGE_PASS, 6, 20), 5, 22);
        }

        @Test
        @DisplayName("gagne 3 points quand il reste 5 jours")
        void gainsThreeQualityWhenFiveDaysRemain() {
            assertItem(afterOneDay(BACKSTAGE_PASS, 5, 20), 4, 23);
        }

        @Test
        @DisplayName("gagne 3 points le dernier jour avant le concert")
        void gainsThreeQualityOnTheLastDayBeforeTheConcert() {
            assertItem(afterOneDay(BACKSTAGE_PASS, 1, 20), 0, 23);
        }

        @Test
        @DisplayName("tombe à 0 le jour du concert")
        void qualityDropsToZeroOnTheDayOfTheConcert() {
            assertItem(afterOneDay(BACKSTAGE_PASS, 0, 20), -1, 0);
        }

        @Test
        @DisplayName("reste à 0 après le concert")
        void qualityStaysAtZeroAfterTheConcert() {
            assertItem(afterOneDay(BACKSTAGE_PASS, -1, 30), -2, 0);
        }

        @Test
        @DisplayName("plafonne à 50 au lieu de dépasser quand il gagne 3 points depuis 49")
        void qualityCapsAtFiftyInsteadOfOvershootingFromFortyNine() {
            assertItem(afterOneDay(BACKSTAGE_PASS, 5, 49), 4, 50);
        }

        @Test
        @DisplayName("ne dépasse jamais 50")
        void qualityNeverExceedsFifty() {
            assertItem(afterOneDay(BACKSTAGE_PASS, 10, 50), 9, 50);
        }
    }

    @Nested
    @DisplayName("Article invoqué (fonctionnalité non encore implémentée)")
    class ConjuredItem {

        @Test
        @DisplayName("se comporte aujourd'hui comme un article ordinaire : -1 point par jour")
        void currentlyBehavesLikeANormalItemBeforeSellByDate() {
            assertItem(afterOneDay(CONJURED_ITEM, 3, 6), 2, 5);
        }

        @Test
        @DisplayName("se comporte aujourd'hui comme un article ordinaire : -2 points une fois périmé")
        void currentlyBehavesLikeANormalItemAfterSellByDate() {
            assertItem(afterOneDay(CONJURED_ITEM, 0, 6), -1, 4);
        }
    }

    @Nested
    @DisplayName("Parcours de l'inventaire")
    class Inventory {

        @Test
        @DisplayName("met à jour tous les articles du tableau")
        void updatesEveryItemOfTheArray() {
            Item normalItem = new Item(NORMAL_ITEM, 10, 20);
            Item agedBrie = new Item(AGED_BRIE, 5, 10);
            Item sulfuras = new Item(SULFURAS, 0, 80);
            GildedRose app = new GildedRose(new Item[] { normalItem, agedBrie, sulfuras });

            app.updateQuality();

            assertAll(
                    () -> assertItem(normalItem, 9, 19),
                    () -> assertItem(agedBrie, 4, 11),
                    () -> assertItem(sulfuras, 0, 80));
        }

        @Test
        @DisplayName("accepte un inventaire vide sans échouer")
        void acceptsAnEmptyInventory() {
            new GildedRose(new Item[0]).updateQuality();
        }
    }

    /** Fait passer une journée sur un article isolé et renvoie son état résultant. */
    private static Item afterOneDay(String name, int sellIn, int quality) {
        Item item = new Item(name, sellIn, quality);
        new GildedRose(new Item[] { item }).updateQuality();
        return item;
    }

    private static void assertItem(Item item, int expectedSellIn, int expectedQuality) {
        assertAll(
                () -> assertEquals(expectedSellIn, item.sellIn, "sellIn"),
                () -> assertEquals(expectedQuality, item.quality, "quality"));
    }
}
