package ru.javarush.simulation;

import org.junit.jupiter.api.Test;
import ru.javarush.config.AnimalSettings;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.config.IslandSettings;
import ru.javarush.config.StopCondition;
import ru.javarush.domain.Island;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationRunnerTest {

    @Test
    void stopsAfterFirstTickWhenNoAnimals() {
        var plant = new AnimalSettings("Растения", 1.0, 200, 0, 0.0, "PLANT", null);
        var islandSettings = new IslandSettings(
                2,
                2,
                500L,
                Map.of(),
                new StopCondition("ALL_ANIMALS_DEAD"),
                null,
                null);
        var cfg = new IslandSimulationConfig(islandSettings, Map.of("plant", plant), Map.of());
        Island island = new Island(2, 2);
        var context = new SimulationContext(island, cfg, new java.util.Random(0));
        var engine = SimulationEngine.withDefaultPhases(context);

        long ticks = new SimulationRunner().run(engine, 10_000);

        assertEquals(1, ticks);
    }
}
