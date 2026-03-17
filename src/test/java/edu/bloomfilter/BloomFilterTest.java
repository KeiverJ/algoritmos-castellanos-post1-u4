package edu.bloomfilter;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para BloomFilter con JUnit 5.
 * Verifica cero falsos negativos y tasa de FP dentro del rango teórico.
 */
class BloomFilterTest {

    @Test
    void testSinFalsosNegativos() {
        BloomFilter<String> filter = new BloomFilter<>(1000, 0.01,
                String::hashCode);
        List<String> elements = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            elements.add("element-" + i);
            filter.add("element-" + i);
        }
        for (String e : elements) {
            assertTrue(filter.mightContain(e),
                    "Falso negativo detectado para: " + e);
        }
    }

    @Test
    void testN1() {
        BloomFilter<String> filter = new BloomFilter<>(1, 0.01,
                String::hashCode);
        filter.add("solo");
        assertTrue(filter.mightContain("solo"));
        assertEquals(1, filter.getHashCount() >= 1 ? 1 : 0);
    }

    @Test
    void testParametrosOptimos() {
        BloomFilter<String> filter = new BloomFilter<>(1000, 0.01,
                String::hashCode);
        assertTrue(filter.getBitCount() > 0);
        assertTrue(filter.getHashCount() >= 1);
        assertTrue(filter.memoryBytes() > 0);
    }

    @Test
    void testTasaFPDentroDelRango() {
        int n = 100_000;
        double epsilon = 0.01;
        BloomFilter<String> filter = new BloomFilter<>(n, epsilon,
                String::hashCode);
        List<String> inserted = new ArrayList<>();
        for (int i = 0; i < n; i++)
            inserted.add("item-" + i);

        double fpRate = BloomFilterValidator.measureFPRate(filter,
                inserted, 10_000);

        // La tasa empírica no debe superar el doble de epsilon
        assertTrue(fpRate <= epsilon * 2,
                String.format("FP rate %.4f supera 2x epsilon %.4f",
                        fpRate, epsilon * 2));
    }

    @Test
    void testMemoriaConsistente() {
        BloomFilter<String> filter = new BloomFilter<>(1000, 0.01,
                String::hashCode);
        assertEquals((long) Math.ceil(filter.getBitCount() / 8.0),
                filter.memoryBytes());
    }
}