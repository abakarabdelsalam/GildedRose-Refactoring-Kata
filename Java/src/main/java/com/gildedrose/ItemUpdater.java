package com.gildedrose;

/**
 * Règle de mise à jour quotidienne applicable à un type d'article.
 * <p>
 * Une implémentation applique la journée complète pour l'article reçu : variation de
 * qualité, écoulement du délai de vente et traitement de la péremption. Ajouter un
 * type d'article consiste à écrire une implémentation et à l'enregistrer dans
 * {@link ItemUpdaters}, sans modifier aucune règle existante.
 */
interface ItemUpdater {

    void update(Item item);

    /**
     * Indique si la date de vente est dépassée. À évaluer après le décrément de
     * {@code sellIn} : un article auquel il restait zéro jour est donc traité comme
     * périmé le jour même.
     */
    default boolean isExpired(Item item) {
        return item.sellIn < 0;
    }
}
