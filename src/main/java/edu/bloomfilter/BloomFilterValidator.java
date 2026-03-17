package edu.bloomfilter;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Utilidad para medir empíricamente la tasa de falsos positivos
 * de un Bloom filter.
 *
 * <p>
 * Inserta un conjunto de elementos conocidos y luego consulta
 * elementos que definitivamente no están insertados para contar
 * cuántos son reportados erróneamente como presentes.
 */
public class BloomFilterValidator {

    /**
     * Mide la tasa empírica de falsos positivos del filtro.
     *
     * <p>
     * Inserta todos los elementos de {@code inserted} en el filtro
     * y luego genera {@code queryCount} strings distintos que no están
     * en el conjunto insertado. Cuenta cuántos son reportados como
     * presentes por el filtro (falsos positivos).
     *
     * <p>
     * Complejidad: O(n + queryCount) donde n = inserted.size().
     *
     * @param filter     Bloom filter ya configurado (vacío al llamar)
     * @param inserted   lista de elementos a insertar en el filtro
     * @param queryCount número de queries negativas a generar
     * @return tasa empírica de falsos positivos en [0.0, 1.0]
     * @pre filter no contiene elementos previos al llamar este método
     * @post retorna fpCount / queryCount donde fpCount son los FP observados
     */
    public static double measureFPRate(BloomFilter<String> filter,
            List<String> inserted,
            int queryCount) {
        // Insertar todos los elementos en el filtro
        inserted.forEach(filter::add);

        // Conjunto de referencia para verificar que los queries son negativos
        Set<String> insertedSet = new HashSet<>(inserted);

        long fpCount = 0;
        long total = 0;
        Random rng = new Random(42);

        while (total < queryCount) {
            // Generar queries que definitivamente NO están en el filtro
            String query = "query_" + rng.nextLong();
            if (!insertedSet.contains(query)) {
                if (filter.mightContain(query))
                    fpCount++;
                total++;
            }
        }
        return (double) fpCount / total;
    }

    /**
     * Estima el uso de memoria de un HashSet con n entradas de String.
     *
     * <p>
     * Cada entrada en HashSet ocupa aproximadamente:
     * 32 bytes (objeto String) + 16 bytes (Entry) + 4 bytes (referencia) = ~52
     * bytes.
     * Se usa 32 bytes como estimación conservadora del overhead por entrada.
     *
     * @param n número de elementos en el HashSet
     * @return estimación de bytes ocupados
     */
    public static long estimateHashSetMemory(int n) {
        return (long) n * 32;
    }
}