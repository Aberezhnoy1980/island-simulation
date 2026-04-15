package ru.javarush.simulation;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;

/**
 * Выполняет один тик как последовательность фаз {@link LifecyclePhase}.
 */
public final class SimulationEngine {

    public static SimulationEngine withDefaultPhases(SimulationContext context) {
        return new SimulationEngine(
                context,
                List.of(
                        new PlantGrowthPhase(),
                        new MovementPhase(),
                        new FeedingPhase(),
                        new ReproductionPhase(),
                        new DeathPhase()));
    }

    private final SimulationContext context;
    private final List<LifecyclePhase> phases;

    public SimulationEngine(SimulationContext context, List<LifecyclePhase> phases) {
        this.context = Objects.requireNonNull(context, "context");
        this.phases = List.copyOf(Objects.requireNonNull(phases, "phases"));
    }

    public SimulationContext context() {
        return context;
    }

    public List<LifecyclePhase> phasesView() {
        return phases;
    }

    /** Одна итерация симуляции: все фазы по порядку, затем увеличение счётчика тиков. */
    public void tick() {
        tickWithStats();
    }

    /**
     * Выполняет тик и возвращает метрики исполнения по фазам.
     */
    public TickExecution tickWithStats() {
        int before = context.island().totalCreatures();
        Map<String, Long> phaseNanos = new LinkedHashMap<>();
        for (LifecyclePhase phase : phases) {
            long start = System.nanoTime();
            phase.execute(context);
            phaseNanos.put(phase.id(), System.nanoTime() - start);
        }
        context.advanceTick();
        int after = context.island().totalCreatures();
        return new TickExecution(
                context.completedTicks(),
                Collections.unmodifiableMap(new LinkedHashMap<>(phaseNanos)),
                before,
                after);
    }
}
