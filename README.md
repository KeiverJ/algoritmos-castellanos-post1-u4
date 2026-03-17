# Bloom Filter vs HashSet — Benchmark

**Curso:** Diseño de Algoritmos y Sistemas — Unidad 4  
**Actividad:** Post-Contenido 1  
**Programa:** Ingeniería de Sistemas — Universidad de Santander (UDES)  
**Año:** 2026

---

## Descripción

Se implementa desde cero un Bloom filter parametrizable en Java 17+
y se compara contra HashSet<String> usando JMH, midiendo throughput
de consulta, uso de memoria estimado y tasa real de falsos positivos.
El objetivo es validar empíricamente el trade-off espacio-exactitud
de las estructuras probabilísticas.

---

## Requisitos

- Java 17+
- Maven 3.8+

```bash
java --version
mvn --version
```

---

## Instrucciones de build y ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/<usuario>/algoritmos-castellanos-post1-u4.git
cd algoritmos-castellanos-post1-u4

# 2. Compilar y ejecutar tests
mvn clean test

# 3. Empaquetar
mvn clean package

# 4. Ejecutar benchmark JMH
java -jar target/benchmarks.jar -rf text -rff results.txt
```

---

## Parámetros del Bloom Filter

Para n = 1.000.000 elementos y ε = 1%:

| Parámetro  | Valor     | Fórmula               |
| ---------- | --------- | --------------------- |
| m (bits)   | 9.585.058 | ceil(-n·ln(ε)/ln(2)²) |
| k (hashes) | 7         | round((m/n)·ln(2))    |
| Memoria    | ~1.2 MB   | ceil(m/8) bytes       |

---

## Resultados del Benchmark JMH

Throughput medido en ops/ms (operaciones por milisegundo).
5 iteraciones de medición, 3 de warmup, 1 fork, n = 1.000.000.

### Consulta (mightContain vs contains)

| Estructura  | Throughput (ops/ms)      | Error (±) |
| ----------- | ------------------------ | --------- |
| BloomFilter | 22.287                   | 430.709   |
| HashSet     | 26.259                   | 264.243   |
| Factor      | HashSet 1.18x más rápido | —         |

### Comparación de memoria

| Estructura  | Memoria estimada | Factor   |
| ----------- | ---------------- | -------- |
| BloomFilter | ~1.2 MB          | 1x base  |
| HashSet     | ~32 MB           | ~27x más |

### Tasa de falsos positivos

| Métrica          | Valor   |
| ---------------- | ------- |
| ε teórico        | 1.00%   |
| FP empírico      | ≤ 2.00% |
| Falsos negativos | 0       |

---

## Análisis de resultados

### Throughput

HashSet es aproximadamente 1.18x más rápido que BloomFilter en
consultas (26.259 vs 22.287 ops/ms). Esta diferencia se explica
porque HashSet realiza una sola operación de hash y una comparación
de referencia O(1), mientras que BloomFilter ejecuta k=7 operaciones
de hash independientes por consulta. Sin embargo, la diferencia de
rendimiento es modesta considerando que BloomFilter usa 27x menos
memoria.

### Memoria

BloomFilter ocupa aproximadamente 1.2 MB para 1 millón de elementos
con ε=1%, mientras que HashSet requiere aproximadamente 32 MB para
almacenar las mismas referencias de String. Esta diferencia de ~27x
en uso de memoria es la principal ventaja del Bloom filter en
aplicaciones con restricciones de memoria o donde se trabaja con
conjuntos extremadamente grandes.

### Falsos positivos

La tasa empírica de falsos positivos se mantiene dentro del rango
teórico esperado (≤ 2× ε = 2%). El filtro garantiza cero falsos
negativos: todo elemento insertado es siempre reportado como presente.
Los falsos positivos son aceptables en aplicaciones donde una
verificación secundaria más costosa solo se realiza cuando el filtro
reporta presencia.

---

## Conclusión: cuándo usar cada estructura

| Criterio                    | BloomFilter       | HashSet               |
| --------------------------- | ----------------- | --------------------- |
| Memoria disponible limitada | Recomendado       | Costoso               |
| Exactitud requerida al 100% | Falsos positivos  | Exacto                |
| Throughput de consulta      | Alta (22K ops/ms) | Más alta (26K ops/ms) |
| Conjunto muy grande (>10M)  | Escalable         | Puede agotar RAM      |
| Caché de pre-filtrado       | Ideal             | Excesivo              |

**BloomFilter** es la elección correcta cuando la memoria es limitada,
se tolera una pequeña tasa de falsos positivos, y el conjunto es
demasiado grande para caber en memoria exacta. Casos de uso típicos:
detección de URLs maliciosas en navegadores, filtros de spam, y
sistemas de caché distribuida. **HashSet** es preferible cuando se
requiere exactitud total, el conjunto es de tamaño moderado, o el
costo de un falso positivo (verificación secundaria) supera el ahorro
de memoria.
