package ru.javarush.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Клетка острова: координаты и список организмов на ней.
 */
public final class Location {

    private final int row;
    private final int column;
    private final List<Organism> residents;

    public Location(int row, int column) {
        this.row = row;
        this.column = column;
        this.residents = new ArrayList<>();
    }

    public int row() {
        return row;
    }

    public int column() {
        return column;
    }

    /**
     * Неизменяемый снимок обитателей клетки (порядок не гарантируется как контракт симуляции).
     */
    public List<Organism> residentsView() {
        return Collections.unmodifiableList(residents);
    }

    /**
     * Агрегированные числа по {@code speciesId} на этой клетке.
     */
    public Map<String, Integer> populationCountsBySpecies() {
        Map<String, Integer> counts = new HashMap<>();
        for (Organism o : residents) {
            counts.merge(o.speciesId(), 1, Integer::sum);
        }
        return counts;
    }

    public int countOf(String speciesId) {
        int n = 0;
        for (Organism o : residents) {
            if (o.speciesId().equals(speciesId)) {
                n++;
            }
        }
        return n;
    }

    public void add(Organism organism) {
        Objects.requireNonNull(organism, "organism");
        residents.add(organism);
    }

    public boolean remove(Organism organism) {
        return residents.remove(organism);
    }

    public int totalCreatures() {
        return residents.size();
    }
}
