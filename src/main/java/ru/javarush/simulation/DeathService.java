package ru.javarush.simulation;

import ru.javarush.config.IslandSettings;
import ru.javarush.domain.Animal;
import ru.javarush.domain.Island;
import ru.javarush.domain.Location;
import ru.javarush.domain.Organism;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Смерть от голода: после учёта приёма пищи за тик удаляются животные с
 * {@code ticksWithoutFood >= limit}. Растения не голодают.
 */
public final class DeathService {

    public static final int DEFAULT_MAX_TICKS_WITHOUT_FOOD = 10;

    public void applyStarvation(Island island, IslandSettings islandSettings) {
        applyStarvation(island, islandSettings, false);
    }

    public void applyStarvation(Island island, IslandSettings islandSettings, boolean parallelCellPasses) {
        Objects.requireNonNull(island, "island");
        Objects.requireNonNull(islandSettings, "islandSettings");

        int limit = islandSettings.maxTicksWithoutFood() != null
                ? islandSettings.maxTicksWithoutFood()
                : DEFAULT_MAX_TICKS_WITHOUT_FOOD;
        if (limit <= 0) {
            return;
        }

        int height = island.height();
        int width = island.width();
        if (parallelCellPasses) {
            IntStream.range(0, height)
                    .parallel()
                    .forEach(row -> processRow(island, row, width, limit));
            return;
        }
        for (int row = 0; row < height; row++) {
            processRow(island, row, width, limit);
        }
    }

    private static void processRow(Island island, int row, int width, int limit) {
        for (int col = 0; col < width; col++) {
            Location cell = island.cell(row, col);
            updateHungerOnCell(cell);
            removeStarvedOnCell(cell, limit);
        }
    }

    private static void updateHungerOnCell(Location cell) {
        for (Organism o : cell.residentsView()) {
            if (o instanceof Animal animal) {
                animal.recordHungerAfterFeedingPhase();
            }
        }
    }

    private static void removeStarvedOnCell(Location cell, int limit) {
        List<Animal> toRemove = new ArrayList<>();
        for (Organism o : cell.residentsView()) {
            if (o instanceof Animal animal && animal.ticksWithoutFood() >= limit) {
                toRemove.add(animal);
            }
        }
        for (Animal animal : toRemove) {
            cell.remove(animal);
        }
    }
}
