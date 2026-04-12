package ru.javarush.domain;

import org.junit.jupiter.api.Test;
import ru.javarush.config.IslandConfigLoader;
import ru.javarush.config.IslandSimulationConfig;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IslandBuilderTest {

    @Test
    void gridDimensionsMatchConfig() {
        IslandSimulationConfig cfg = new IslandConfigLoader().loadDefault();
        Island island = new IslandBuilder(new Random(1)).build(cfg);

        assertEquals(cfg.island().width(), island.width());
        assertEquals(cfg.island().height(), island.height());
    }

    @Test
    void initialPopulationsSumMatchesConfig() {
        IslandSimulationConfig cfg = new IslandConfigLoader().loadDefault();
        Island island = new IslandBuilder(new Random(42)).build(cfg);

        var expected = cfg.island().initialAnimals();
        var actual = island.totalPopulationBySpecies();

        for (var entry : expected.entrySet()) {
            assertEquals(entry.getValue(), actual.getOrDefault(entry.getKey(), 0),
                    "species " + entry.getKey());
        }
        assertEquals(
                expected.values().stream().mapToInt(Integer::intValue).sum(),
                island.totalCreatures());
    }

    @Test
    void islandRejectsNonPositiveSize() {
        assertThrows(IllegalArgumentException.class, () -> new Island(0, 10));
        assertThrows(IllegalArgumentException.class, () -> new Island(10, 0));
    }
}
