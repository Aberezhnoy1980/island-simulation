package ru.javarush.simulation;

import org.junit.jupiter.api.Test;
import ru.javarush.config.AnimalSettings;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.config.IslandSettings;
import ru.javarush.config.StopCondition;
import ru.javarush.domain.Island;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduledSimulationRunnerTest {

    @Test
    void stopsAfterFirstTickWhenNoAnimals() {
        var plant = new AnimalSettings("Растения", 1.0, 200, 0, 0.0, "PLANT", null);
        var islandSettings = new IslandSettings(
                2,
                2,
                0L,
                Map.of(),
                new StopCondition("ALL_ANIMALS_DEAD"),
                null,
                null,
                null,
                null);
        var cfg = new IslandSimulationConfig(islandSettings, Map.of("plant", plant), Map.of());
        Island island = new Island(2, 2);
        var context = new SimulationContext(island, cfg, new java.util.Random(0));
        var engine = SimulationEngine.withDefaultPhases(context);

        long ticks = new ScheduledSimulationRunner().runWithExecutionObserver(
                engine,
                10_000,
                1L,
                0L,
                (execution, ctx) -> { });

        assertEquals(1, ticks);
    }

    @Test
    void callsObserverOnConfiguredTickFrequency() {
        var rabbit = new AnimalSettings("Кролик", 2.0, 150, 2, 0.45, "HERBIVORE", null);
        var islandSettings = new IslandSettings(
                2,
                2,
                0L,
                Map.of(),
                new StopCondition("NONE"),
                null,
                null,
                null,
                null);
        var cfg = new IslandSimulationConfig(islandSettings, Map.of("rabbit", rabbit), Map.of());
        Island island = new Island(2, 2);
        var context = new SimulationContext(island, cfg, new java.util.Random(0));
        var engine = new SimulationEngine(context, java.util.List.of());
        AtomicLong calls = new AtomicLong();

        long ticks = new ScheduledSimulationRunner().runWithExecutionObserver(
                engine,
                10L,
                1L,
                3L,
                (execution, ctx) -> calls.incrementAndGet());

        assertEquals(10, ticks);
        assertEquals(3, calls.get());
    }
}
