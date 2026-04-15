package ru.javarush.simulation;

import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.domain.Animal;
import ru.javarush.domain.Island;
import ru.javarush.domain.Location;
import ru.javarush.domain.Organism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Питание на клетке: для каждого животного (в случайном порядке) читается строка {@code dietMatrix},
 * по вероятности {@code 0..100} (успех, если {@code nextInt(100) < chance}) съедается жертва с клетки.
 * Растения не охотятся.
 * <p>
 * Жертва всегда исчезает с клетки целиком. Если её масса помещается в остаток лимита за тик — учёт по весу;
 * если масса больше (например растение 1&nbsp;кг при лимите кролика 0.45&nbsp;кг) — охотник всё равно
 * насыщается до {@code maxFoodKg} за тик, «недоеденных огрызков» нет.
 */
public final class FeedingService {

    public void feedAll(Island island, IslandSimulationConfig config, Random random) {
        feedAll(island, config, random, false);
    }

    public void feedAll(Island island, IslandSimulationConfig config, Random random, boolean parallelCellPasses) {
        Objects.requireNonNull(island, "island");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(random, "random");

        resetStomachs(island, parallelCellPasses);

        int height = island.height();
        int width = island.width();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                feedAtCell(island.cell(row, col), config, random);
            }
        }
    }

    private static void resetStomachs(Island island, boolean parallelCellPasses) {
        int height = island.height();
        int width = island.width();
        if (parallelCellPasses) {
            IntStream.range(0, height)
                    .parallel()
                    .forEach(row -> resetStomachsOnRow(island, row, width));
            return;
        }
        for (int row = 0; row < height; row++) {
            resetStomachsOnRow(island, row, width);
        }
    }

    private static void resetStomachsOnRow(Island island, int row, int width) {
        for (int col = 0; col < width; col++) {
            for (Organism o : island.cell(row, col).residentsView()) {
                if (o instanceof Animal animal) {
                    animal.startFeedingRound();
                }
            }
        }
    }

    private void feedAtCell(Location cell, IslandSimulationConfig config, Random random) {
        List<Animal> hunters = new ArrayList<>();
        for (Organism o : cell.residentsView()) {
            if (o instanceof Animal a) {
                hunters.add(a);
            }
        }
        Collections.shuffle(hunters, random);

        for (Animal hunter : hunters) {
            Map<String, Integer> diet = config.dietMatrix().get(hunter.speciesId());
            if (diet == null || diet.isEmpty()) {
                continue;
            }
            while (tryEatOneMeal(cell, hunter, diet, random)) {
                // пока есть удачные охоты и запас maxFoodKg
            }
        }
    }

    /**
     * Одна попытка: перебираются кандидаты в случайном порядке; при успешном броске жертва снимается с клетки.
     *
     * @return {@code true}, если съели одну жертву
     */
    private boolean tryEatOneMeal(Location cell, Animal hunter, Map<String, Integer> diet, Random random) {
        if (hunter.isFullySatiatedThisTick()) {
            return false;
        }
        List<Organism> candidates = new ArrayList<>();
        for (Organism prey : cell.residentsView()) {
            if (prey == hunter) {
                continue;
            }
            Integer chance = diet.get(prey.speciesId());
            if (chance == null || chance <= 0) {
                continue;
            }
            candidates.add(prey);
        }
        if (candidates.isEmpty()) {
            return false;
        }
        Collections.shuffle(candidates, random);

        for (Organism prey : candidates) {
            int chance = diet.get(prey.speciesId());
            if (random.nextInt(100) >= chance) {
                continue;
            }
            double preyKg = prey.settings().weightKg();
            if (hunter.canConsumePreyWeight(preyKg)) {
                hunter.registerMeal(preyKg);
            } else {
                hunter.saturateAfterEatingEntireOversizedPrey();
            }
            cell.remove(prey);
            return true;
        }
        return false;
    }
}
