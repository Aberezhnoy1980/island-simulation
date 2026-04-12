package ru.javarush.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Клетка острова: координаты и численность по идентификатору вида (как в конфиге YAML).
 */
public final class Location {

    private final int row;
    private final int column;
    private final Map<String, Integer> population;

    public Location(int row, int column) {
        this.row = row;
        this.column = column;
        this.population = new HashMap<>();
    }

    public int row() {
        return row;
    }

    public int column() {
        return column;
    }

    /**
     * Неизменяемый снимок текущих чисел на клетке.
     */
    public Map<String, Integer> populationView() {
        return Collections.unmodifiableMap(population);
    }

    public int countOf(String speciesId) {
        return population.getOrDefault(speciesId, 0);
    }

    public void add(String speciesId, int delta) {
        Objects.requireNonNull(speciesId, "speciesId");
        if (delta == 0) {
            return;
        }
        population.merge(speciesId, delta, Integer::sum);
        population.computeIfPresent(speciesId, (k, v) -> v <= 0 ? null : v);
    }

    public int totalCreatures() {
        return population.values().stream().mapToInt(Integer::intValue).sum();
    }
}
