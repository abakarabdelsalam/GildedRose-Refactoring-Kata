package com.gildedrose;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test de caractérisation global : rejoue la simulation de {@link TexttestFixture}
 * et compare la sortie complète à une référence capturée sur le code d'origine,
 * avant tout refactoring.
 * <p>
 * Ce test ne décrit aucune règle métier en particulier : il constate l'ensemble du
 * comportement observable. Tant qu'il passe, aucun refactoring n'a changé le
 * comportement du système.
 */
@DisplayName("Golden master")
class GoldenMasterTest {

    private static final String REFERENCE_FILE = "/golden-master-30-days.txt";

    private static final String DAYS = "30";

    @Test
    @DisplayName("la simulation de 30 jours reproduit exactement la sortie de référence")
    void simulationMatchesReferenceOutput() throws IOException {
        assertEquals(readReference(), runFixture(DAYS));
    }

    private static String runFixture(String days) throws IOException {
        PrintStream standardOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8.name()));
            TexttestFixture.main(new String[] { days });
        } finally {
            System.setOut(standardOut);
        }
        return normalizeLineEndings(new String(captured.toByteArray(), StandardCharsets.UTF_8));
    }

    private static String readReference() throws IOException {
        try (InputStream reference = GoldenMasterTest.class.getResourceAsStream(REFERENCE_FILE)) {
            assertNotNull(reference, "fichier de référence introuvable : " + REFERENCE_FILE);
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = reference.read(chunk)) != -1) {
                content.write(chunk, 0, read);
            }
            return normalizeLineEndings(new String(content.toByteArray(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Rend la comparaison indépendante du système d'exploitation : {@code println}
     * produit \r\n sous Windows et \n sous Linux et macOS.
     */
    private static String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n");
    }
}
