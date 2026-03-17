package edu.bloomfilter;

import java.util.BitSet;
import java.util.function.ToIntFunction;

/**
 * Bloom filter parametrizable con k funciones hash por double-hashing.
 *
 * <p>Estructura probabilística que permite consultas de membresía con
 * cero falsos negativos y una tasa controlada de falsos positivos ε.
 * Usa un BitSet de m bits y k funciones hash derivadas por double-hashing
 * a partir de una función hash base.
 *
 * <p>Parámetros óptimos calculados automáticamente:
 * <ul>
 *   <li>m = ceil(-n * ln(ε) / ln(2)²) — tamaño del arreglo de bits</li>
 *   <li>k = round((m/n) * ln(2)) — número de funciones hash</li>
 * </ul>
 *
 * @param <T> tipo de elementos a insertar
 */
public class BloomFilter<T> {

    /** Arreglo de bits del filtro. */
    private final BitSet bits;

    /** Tamaño del arreglo de bits (m). */
    private final int m;

    /** Número de funciones hash (k). */
    private final int k;

    /** Función hash base proporcionada por el cliente. */
    private final ToIntFunction<T> hashFn;

    /**
     * Construye un Bloom filter óptimo para n elementos y tasa de FP epsilon.
     *
     * <p>Complejidad: O(m) para inicializar el BitSet.
     *
     * @param n       número esperado de elementos a insertar (n > 0)
     * @param epsilon tasa deseada de falsos positivos (0 < epsilon < 1)
     * @param hashFn  función hash base para los elementos
     * @pre  n > 0 y 0 < epsilon < 1
     * @post el filtro está listo para inserción con parámetros m y k óptimos
     */
    public BloomFilter(int n, double epsilon, ToIntFunction<T> hashFn) {
        this.m = (int) Math.ceil(-n * Math.log(epsilon)
                / (Math.log(2) * Math.log(2)));
        this.k = (int) Math.max(1, Math.round((double) m / n * Math.log(2)));
        this.bits = new BitSet(m);
        this.hashFn = hashFn;
    }

    /**
     * Calcula la posición del bit para la i-ésima función hash.
     * Usa double-hashing: h_i(x) = (h1(x) + i * h2(x)) mod m
     *
     * @param h1 primer hash del elemento
     * @param h2 segundo hash derivado (impar para cubrir todas las posiciones)
     * @param i  índice de la función hash (0 <= i < k)
     * @return   posición en el arreglo de bits
     */
    private int hash(int h1, int h2, int i) {
        return Math.floorMod(h1 + i * h2, m);
    }

    /**
     * Inserta el elemento en el filtro activando k bits.
     *
     * <p>Complejidad: O(k) — k operaciones de hash y set de bits.
     *
     * @param element elemento a insertar (no nulo)
     * @post  los k bits correspondientes al elemento están activados
     */
    public void add(T element) {
        int h1 = hashFn.applyAsInt(element);
        int h2 = Integer.reverse(h1) | 1; // h2 impar para cubrir todas las posiciones
        for (int i = 0; i < k; i++) {
            bits.set(hash(h1, h2, i));
        }
    }

    /**
     * Consulta si el elemento posiblemente está en el conjunto.
     *
     * <p>Complejidad: O(k) — k operaciones de hash y get de bits.
     *
     * @param element elemento a consultar (no nulo)
     * @return true si el elemento posiblemente está (puede ser falso positivo),
     *         false si definitivamente no está (nunca falso negativo)
     */
    public boolean mightContain(T element) {
        int h1 = hashFn.applyAsInt(element);
        int h2 = Integer.reverse(h1) | 1;
        for (int i = 0; i < k; i++) {
            if (!bits.get(hash(h1, h2, i))) return false;
        }
        return true;
    }

    /**
     * Retorna el tamaño del arreglo de bits m.
     *
     * @return número de bits del filtro
     */
    public int getBitCount() { return m; }

    /**
     * Retorna el número de funciones hash k.
     *
     * @return número de funciones hash
     */
    public int getHashCount() { return k; }

    /**
     * Estima el uso de memoria del filtro en bytes.
     *
     * @return número de bytes ocupados por el BitSet
     */
    public long memoryBytes() { return (long) Math.ceil(m / 8.0); }
}