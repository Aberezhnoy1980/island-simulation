package ru.javarush.simulation;

import ru.javarush.config.AnimalSettings;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.domain.Island;
import ru.javarush.domain.OrganismFactory;
import ru.javarush.domain.OrganismKind;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * На каждой клетке с вероятностью из конфига добавляется одна особь вида {@code PLANT}, если ещё не достигнут {@code maxPerLocation}.
 */
public final class PlantGrowthService {

    public static final int DEFAULT_PLANT_GROWTH_CHANCE_PERCENT = 25;

    public void grow(Island island, IslandSimulationConfig config, Random random) {
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

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                var cell = island.cell(row, col);
                if (cell.countOf(plantSpeciesId) >= plantSettings.maxPerLocation()) {
                    continue;
                }
                if (random.nextInt(100) >= chance) {
                    continue;
                }
                cell.add(factory.create(plantSpeciesId));
            }
        }
    }

    static String resolvePlantSpeciesId(IslandSimulationConfig config) {
        return config.animals().entrySet().stream()
                .filter(e -> OrganismKind.PLANT.name().equalsIgnoreCase(e.getValue().type()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
