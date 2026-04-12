package ru.javarush.simulation;

import java.util.List;
import java.util.Objects;

/**
 * Выполняет один тик как последовательность фаз {@link LifecyclePhase}.
 */
public final class SimulationEngine {

    public static SimulationEngine withDefaultPhases(SimulationContext context) {
        return new SimulationEngine(
                context,
                List.of(
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
        for (LifecyclePhase phase : phases) {
            phase.execute(context);
        }
        context.advanceTick();
    }
}
