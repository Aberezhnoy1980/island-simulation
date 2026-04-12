package ru.javarush.simulation;

import ru.javarush.config.IslandSettings;
import ru.javarush.domain.Animal;
import ru.javarush.domain.Island;
import ru.javarush.domain.Location;
import ru.javarush.domain.Organism;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Смерть от голода: после учёта приёма пищи за тик удаляются животные с
 * {@code ticksWithoutFood >= limit}. Растения не голодают.
 */
public final class DeathService {

    public static final int DEFAULT_MAX_TICKS_WITHOUT_FOOD = 10;

    public void applyStarvation(Island island, IslandSettings islandSettings) {
        Objects.requireNonNull(island, "island");
        Objects.requireNonNull(islandSettings, "islandSettings");

        int limit = islandSettings.maxTicksWithoutFood() != null
                ? islandSettings.maxTicksWithoutFood()
                : DEFAULT_MAX_TICKS_WITHOUT_FOOD;
        if (limit <= 0) {
            return;
        }

        for (int row = 0; row < island.height(); row++) {
            for (int col = 0; col < island.width(); col++) {
                Location cell = island.cell(row, col);
                updateHungerOnCell(cell);
                removeStarvedOnCell(cell, limit);
            }
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
