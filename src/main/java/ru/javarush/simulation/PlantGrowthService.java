package ru.javarush.simulation;

import ru.javarush.config.AnimalSettings;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.domain.Island;
import ru.javarush.domain.OrganismFactory;
import ru.javarush.domain.OrganismKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * На каждой клетке с вероятностью из конфига добавляется одна особь вида {@code PLANT}, если ещё не достигнут {@code maxPerLocation}.
 */
public final class PlantGrowthService {

    public static final int DEFAULT_PLANT_GROWTH_CHANCE_PERCENT = 25;

    public void grow(Island island, IslandSimulationConfig config, Random random) {
        grow(island, config, random, false);
    }

    public void grow(Island island, IslandSimulationConfig config, Random random, boolean parallelPlanning) {
        Objects.requireNonNull(island, "island");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(random, "random");

        String plantSpeciesId = resolvePlantSpeciesId(config);
        if (plantSpeciesId == null) {
            return;
        }
        AnimalSettings plantSettings = config.animals().get(plantSpeciesId);
        if (plantSettings == null) {
            return;
        }

        int chance = config.island().plantGrowthChancePercent() != null
                ? config.island().plantGrowthChancePercent()
                : DEFAULT_PLANT_GROWTH_CHANCE_PERCENT;
        if (chance <= 0) {
            return;
        }

        OrganismFactory factory = new OrganismFactory(config);
        int height = island.height();
        int width = island.width();

        List<PlantSpawn> plan;
        if (parallelPlanning) {
            plan = IntStream.range(0, height)
                    .parallel()
                    .mapToObj(row -> planForRow(island, row, width, plantSpeciesId, plantSettings.maxPerLocation(), chance, true, random))
                    .flatMap(List::stream)
                    .toList();
        } else {
            plan = new ArrayList<>();
            for (int row = 0; row < height; row++) {
                plan.addAll(planForRow(island, row, width, plantSpeciesId, plantSettings.maxPerLocation(), chance, false, random));
            }
        }

        for (PlantSpawn spawn : plan) {
            var cell = island.cell(spawn.row(), spawn.col());
            if (cell.countOf(plantSpeciesId) >= plantSettings.maxPerLocation()) {
                continue;
            }
            cell.add(factory.create(plantSpeciesId));
        }
    }

    private static List<PlantSpawn> planForRow(
            Island island,
            int row,
            int width,
            String plantSpeciesId,
            int maxPerLocation,
            int chance,
            boolean parallelRandom,
            Random random
    ) {
        List<PlantSpawn> rowPlan = new ArrayList<>();
        for (int col = 0; col < width; col++) {
            var cell = island.cell(row, col);
            if (cell.countOf(plantSpeciesId) >= maxPerLocation) {
                continue;
            }
            Random rng = parallelRandom ? ThreadLocalRandom.current() : random;
            if (rng.nextInt(100) < chance) {
                rowPlan.add(new PlantSpawn(row, col));
            }
        }
        return rowPlan;
    }

    static String resolvePlantSpeciesId(IslandSimulationConfig config) {
        return config.animals().entrySet().stream()
                .filter(e -> OrganismKind.PLANT.name().equalsIgnoreCase(e.getValue().type()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private record PlantSpawn(int row, int col) {
    }
}
