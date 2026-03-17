package edu.bloomfilter.bench;

import edu.bloomfilter.BloomFilter;
import edu.bloomfilter.BloomFilterValidator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparativo de BloomFilter vs HashSet.
 *
 * <p>
 * Mide el throughput (ops/ms) de consulta para ambas estructuras
 * con n = 1.000.000 de strings. El setup precarga ambas estructuras
 * con los mismos datos para garantizar comparación justa.
 *
 * <p>
 * Configuración JMH:
 * <ul>
 * <li>Modo: Throughput (ops/ms)</li>
 * <li>Warmup: 3 iteraciones de 1 segundo</li>
 * <li>Medición: 5 iteraciones de 2 segundos</li>
 * <li>Fork: 1 proceso JVM independiente</li>
 * </ul>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class MembershipBenchmark {

    /** Número de elementos insertados en ambas estructuras. */
    static final int N = 1_000_000;

    /** Datos de entrada generados aleatoriamente. */
    List<String> data;

    /** Bloom filter con epsilon = 1%. */
    BloomFilter<String> bloom;

    /** HashSet de referencia exacta. */
    HashSet<String> hashSet;

    /** Array de queries: 500 presentes + 500 ausentes. */
    String[] queries;

    /** Tasa empírica de falsos positivos medida durante setup. */
    double fpRate;

    /** Memoria estimada del BloomFilter en bytes. */
    long bloomMemoryBytes;

    /** Memoria estimada del HashSet en bytes. */
    long hashSetMemoryBytes;

    /**
     * Inicializa ambas estructuras con N elementos antes de cada trial.
     * Genera queries mixtas (mitad presentes, mitad ausentes) y
     * mide la tasa real de falsos positivos.
     */
    @Setup(Level.Trial)
    public void setup() {
        // Generar datos aleatorios
        data = new ArrayList<>(N);
        Random rng = new Random(0);
        for (int i = 0; i < N; i++) {
            data.add("element-" + rng.nextLong());
        }

        // Construir Bloom filter con epsilon = 1%
        bloom = new BloomFilter<>(N, 0.01, s -> s.hashCode());
        data.forEach(bloom::add);

        // Construir HashSet
        hashSet = new HashSet<>(data);

        // Queries: mitad presentes, mitad ausentes
        queries = new String[1000];
        for (int i = 0; i < 500; i++) {
            queries[i] = data.get(i);
        }
        for (int i = 500; i < 1000; i++) {
            queries[i] = "absent-" + rng.nextLong();
        }

        // Medir tasa real de FP con un filtro fresco
        BloomFilter<String> fpFilter = new BloomFilter<>(N, 0.01,
                s -> s.hashCode());
        fpRate = BloomFilterValidator.measureFPRate(fpFilter, data, 10_000);

        // Estimar memoria
        bloomMemoryBytes = bloom.memoryBytes();
        hashSetMemoryBytes = BloomFilterValidator.estimateHashSetMemory(N);

        System.out.printf("%n=== Configuración del Bloom Filter ===%n");
        System.out.printf("m (bits): %d%n", bloom.getBitCount());
        System.out.printf("k (hashes): %d%n", bloom.getHashCount());
        System.out.printf("Memoria BloomFilter: %,d bytes (%.2f MB)%n",
                bloomMemoryBytes, bloomMemoryBytes / 1_048_576.0);
        System.out.printf("Memoria HashSet est: %,d bytes (%.2f MB)%n",
                hashSetMemoryBytes, hashSetMemoryBytes / 1_048_576.0);
        System.out.printf("Tasa FP teórica:   1.00%%%n");
        System.out.printf("Tasa FP empírica:  %.2f%%%n%n", fpRate * 100);
    }

    /**
     * Benchmark de consulta en Bloom filter — O(k) por operación.
     *
     * @return resultado de la consulta (evita dead code elimination)
     */
    @Benchmark
    public boolean bloomQuery() {
        return bloom.mightContain(
                queries[(int) (System.nanoTime() % 1000)]);
    }

    /**
     * Benchmark de consulta en HashSet — O(1) amortizado por operación.
     *
     * @return resultado de la consulta (evita dead code elimination)
     */
    @Benchmark
    public boolean hashSetQuery() {
        return hashSet.contains(
                queries[(int) (System.nanoTime() % 1000)]);
    }
}