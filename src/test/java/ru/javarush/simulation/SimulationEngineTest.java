package ru.javarush.simulation;

import org.junit.jupiter.api.Test;
import ru.javarush.config.IslandConfigLoader;
import ru.javarush.domain.Island;
import ru.javarush.domain.IslandBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationEngineTest {

    @Test
    void runsPhasesInOrderAndAdvancesTickCounter() {
        var cfg = new IslandConfigLoader().loadDefault();
        Island island = new IslandBuilder(new Random(0)).build(cfg);
        var context = new SimulationContext(island, cfg, new Random(0));

        List<String> order = new ArrayList<>();
        List<LifecyclePhase> phases = List.of(
                new RecordingPhase("a", order),
                new RecordingPhase("b", order));

        var engine = new SimulationEngine(context, phases);

        engine.tick();
        engine.tick();

        assertEquals(List.of("a", "b", "a", "b"), order);
        assertEquals(2, context.completedTicks());
    }

    @Test
    void defaultEngineHasFourPhasesInOrder() {
        var cfg = new IslandConfigLoader().loadDefault();
        Island island = new IslandBuilder(new Random(0)).build(cfg);
        var ctx = new SimulationContext(island, cfg, new Random(0));
        var engine = SimulationEngine.withDefaultPhases(ctx);

        assertEquals(
                List.of("movement", "feeding", "reproduction", "death"),
                engine.phasesView().stream().map(LifecyclePhase::id).toList());
    }

    private static final class RecordingPhase implements LifecyclePhase {
        private final String id;
        private final List<String> log;

        RecordingPhase(String id, List<String> log) {
            this.id = id;
            this.log = log;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public void execute(SimulationContext context) {
            log.add(id);
        }
    }
}
