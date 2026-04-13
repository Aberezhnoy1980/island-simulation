package ru.javarush.simulation;

import java.util.Objects;

/**
 * Цикл тиков до срабатывания {@link StopConditionEvaluator} или исчерпания бюджета шагов.
 */
public final class SimulationRunner {

    private final StopConditionEvaluator stopCondition = new StopConditionEvaluator();

    /**
     * @return сколько тиков реально выполнено
     */
    public long run(SimulationEngine engine, long maxTicks) {
        Objects.requireNonNull(engine, "engine");
        if (maxTicks < 0) {
            throw new IllegalArgumentException("maxTicks must be non-negative");
        }

        long executed = 0;
        while (executed < maxTicks) {
            engine.tick();
            executed++;
            if (stopCondition.shouldStop(engine.context().island(), engine.context().config().island().stopCondition())) {
                break;
            }
        }
        return executed;
    }
}
