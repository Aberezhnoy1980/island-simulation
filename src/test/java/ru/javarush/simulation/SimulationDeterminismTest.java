package ru.javarush.simulation;

import org.junit.jupiter.api.Test;
import ru.javarush.config.IslandConfigLoader;
import ru.javarush.domain.Island;
import ru.javarush.domain.IslandBuilder;
import ru.javarush.domain.OrganismKind;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationDeterminismTest {

    @Test
    void sameSeedProducesSamePopulationSnapshot() {
        Snapshot first = runDeterministic(42L, 50);
        Snapshot second = runDeterministic(42L, 50);

        assertEquals(first.executedTicks(), second.executedTicks());
        assertEquals(first.totalCreatures(), second.totalCreatures());
        assertEquals(first.byKind(), second.byKind());
        assertEquals(first.bySpecies(), second.bySpecies());
    }

    private static Snapshot runDeterministic(long seed, long maxTicks) {
        var cfg = new IslandConfigLoader().loadDefault();
        Random random = new Random(seed);
        Island island = new IslandBuilder(random).build(cfg);
        var context = new SimulationContext(island, cfg, random);
        var engine = SimulationEngine.withDefaultPhases(context);
        long executed = new SimulationRunner().run(engine, maxTicks, 0L, 0L, (tick, ctx) -> { });

        return new Snapshot(
                executed,
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
