package ru.javarush.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Остров как сетка {@link Location}. Размеры совпадают с {@code island.width} / {@code island.height} в конфиге.
 */
public final class Island {

    private final int width;
    private final int height;
    private final Location[][] cells;

    public Island(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        this.width = width;
        this.height = height;
        this.cells = new Location[height][width];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                cells[r][c] = new Location(r, c);
            }
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Location cell(int row, int column) {
        Objects.checkIndex(row, height);
        Objects.checkIndex(column, width);
        return cells[row][column];
    }

    /**
     * Суммарная численность по видам по всему острову.
     */
    public Map<String, Integer> totalPopulationBySpecies() {
        Map<String, Integer> totals = new HashMap<>();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                for (var e : cells[r][c].populationView().entrySet()) {
                    totals.merge(e.getKey(), e.getValue(), Integer::sum);
                }
            }
        }
        return totals;
    }

    public int totalCreatures() {
        return Arrays.stream(cells)
                .flatMap(Arrays::stream)
                .mapToInt(Location::totalCreatures)
                .sum();
    }
}
