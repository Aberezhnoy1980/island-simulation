package ru.javarush.simulation;

import org.junit.jupiter.api.Test;
import ru.javarush.config.AnimalSettings;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.config.IslandSettings;
import ru.javarush.config.StopCondition;
import ru.javarush.domain.Island;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlantGrowthServiceTest {

    private final PlantGrowthService growth = new PlantGrowthService();

    private static Random alwaysGrow() {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                if (bound == 100) {
                    return 0;
                }
                return super.nextInt(bound);
            }
        };
    }

    private static IslandSimulationConfig plantOnlyConfig(int growthChance) {
        var plant = new AnimalSettings("Растения", 1.0, 5, 0, 0.0, "PLANT", null);
        var island = new IslandSettings(3, 3, 500L, Map.of(), new StopCondition("NONE"), null, growthChance, null, null);
        return new IslandSimulationConfig(island, Map.of("plant", plant), Map.of());
    }

    @Test
    void addsPlantWhenUnderCapAndRollSucceeds() {
        IslandSimulationConfig cfg = plantOnlyConfig(100);
        Island island = new Island(2, 2);
        growth.grow(island, cfg, alwaysGrow());
        assertEquals(4, island.totalCreatures());
        assertEquals(1, island.cell(0, 0).countOf("plant"));
    }

    @Test
    void respectsMaxPerLocation() {
        IslandSimulationConfig cfg = plantOnlyConfig(100);
        Island island = new Island(1, 1);
        for (int i = 0; i < 30; i++) {
            growth.grow(island, cfg, alwaysGrow());
        }
        assertEquals(5, island.cell(0, 0).countOf("plant"));
    }

    @Test
    void resolvePlantSpeciesIdMissing() {
        var island = new IslandSettings(2, 2, 500L, Map.of(), new StopCondition("NONE"), null, null, null, null);
        var cfg = new IslandSimulationConfig(
                island,
                Map.of("wolf", new AnimalSettings("Волк", 50, 30, 3, 8, "PREDATOR", null)),
                Map.of());
        assertNull(PlantGrowthService.resolvePlantSpeciesId(cfg));
    }

    @Test
    void parallelPlanningRespectsCapacityPerCell() {
        IslandSimulationConfig cfg = plantOnlyConfig(100);
        Island island = new Island(6, 6);

        for (int i = 0; i < 20; i++) {
            growth.grow(island, cfg, new Random(i), true);
        }

        int cap = cfg.animals().get("plant").maxPerLocation();
        for (int row = 0; row < island.height(); row++) {
            for (int col = 0; col < island.width(); col++) {
                assertEquals(cap, island.cell(row, col).countOf("plant"));
            }
        }
    }
}
