package ru.javarush.simulation;

import org.junit.jupiter.api.Test;
import ru.javarush.config.IslandConfigLoader;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.config.IslandSettings;
import ru.javarush.config.StopCondition;
import ru.javarush.domain.Island;
import ru.javarush.domain.IslandBuilder;
import ru.javarush.domain.OrganismKind;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunnerModeParityTest {

    @Test
    void scheduledAndLoopModesProduceSameSnapshotForSameSeed() {
        Snapshot loop = runLoopMode(123L, 80L);
        Snapshot scheduled = runScheduledMode(123L, 80L);

        assertEquals(loop.executedTicks(), scheduled.executedTicks());
        assertEquals(loop.totalCreatures(), scheduled.totalCreatures());
        assertEquals(loop.byKind(), scheduled.byKind());
        assertEquals(loop.bySpecies(), scheduled.bySpecies());
    }

    private static Snapshot runLoopMode(long seed, long maxTicks) {
        var cfg = withStopNone(new IslandConfigLoader().loadDefault());
        Random random = new Random(seed);
        Island island = new IslandBuilder(random).build(cfg);
        var context = new SimulationContext(island, cfg, random);
        var engine = SimulationEngine.withDefaultPhases(context);
        long executed = new SimulationRunner().run(engine, maxTicks, 0L, 0L, (tick, ctx) -> { });
        return snapshot(executed, island);
    }

    private static Snapshot runScheduledMode(long seed, long maxTicks) {
        var cfg = withStopNone(new IslandConfigLoader().loadDefault());
        Random random = new Random(seed);
        Island island = new IslandBuilder(random).build(cfg);
        var context = new SimulationContext(island, cfg, random);
        var engine = SimulationEngine.withDefaultPhases(context);
        long executed = new ScheduledSimulationRunner().runWithExecutionObserver(
                engine,
                maxTicks,
                1L,
                0L,
                (execution, ctx) -> { });
        return snapshot(executed, island);
    }

    private static IslandSimulationConfig withStopNone(IslandSimulationConfig cfg) {
        IslandSettings island = cfg.island();
        var updatedIsland = new IslandSettings(
                island.width(),
                island.height(),
                island.tickDurationMillis(),
                island.initialAnimals(),
                new StopCondition("NONE"),
                island.maxTicksWithoutFood(),
                island.plantGrowthChancePercent(),
                island.parallelMovementPlanning(),
                island.parallelPlantGrowthPlanning());
        return new IslandSimulationConfig(updatedIsland, cfg.animals(), cfg.dietMatrix());
    }

    private static Snapshot snapshot(long executedTicks, Island island) {
        return new Snapshot(
                executedTicks,
                island.totalCreatures(),
                new TreeMap<>(island.totalPopulationByKind()),
                new TreeMap<>(island.totalPopulationBySpecies()));
    }

    private record Snapshot(
            long executedTicks,
            int totalCreatures,
            Map<OrganismKind, Long> byKind,
            Map<String, Integer> bySpecies
    ) {
    }
}
