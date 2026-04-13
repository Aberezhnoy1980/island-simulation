package ru.javarush.simulation;

import java.util.Objects;

/**
 * Цикл тиков до срабатывания {@link StopConditionEvaluator} или исчерпания бюджета шагов.
 */
public final class SimulationRunner {

    private final StopConditionEvaluator stopCondition = new StopConditionEvaluator();
    private static final TickObserver NOOP_OBSERVER = (tick, context) -> { };

    @FunctionalInterface
    public interface TickObserver {
        void onTickCompleted(long tickNumber, SimulationContext context);
    }

    /**
     * @return сколько тиков реально выполнено
     */
    public long run(SimulationEngine engine, long maxTicks) {
        return run(engine, maxTicks, 0L, 0L, NOOP_OBSERVER);
    }

    /**
     * @param tickDelayMillis пауза после каждого тика (0 — без задержки)
     * @param reportEveryTicks если > 0, вызывает observer на каждом N-м тике
     * @return сколько тиков реально выполнено
     */
    public long run(
            SimulationEngine engine,
            long maxTicks,
            long tickDelayMillis,
            long reportEveryTicks,
            TickObserver observer
    ) {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(observer, "observer");
        if (maxTicks < 0) {
            throw new IllegalArgumentException("maxTicks must be non-negative");
        }
        if (tickDelayMillis < 0) {
            throw new IllegalArgumentException("tickDelayMillis must be non-negative");
        }
        if (reportEveryTicks < 0) {
            throw new IllegalArgumentException("reportEveryTicks must be non-negative");
        }

        long executed = 0;
        while (executed < maxTicks) {
            engine.tick();
            executed++;
            if (reportEveryTicks > 0 && executed % reportEveryTicks == 0) {
                observer.onTickCompleted(executed, engine.context());
            }
            if (stopCondition.shouldStop(engine.context().island(), engine.context().config().island().stopCondition())) {
                break;
            }
            if (tickDelayMillis > 0) {
                try {
                    Thread.sleep(tickDelayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return executed;
    }
}
