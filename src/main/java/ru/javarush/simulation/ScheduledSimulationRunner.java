package ru.javarush.simulation;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Запуск тиков через {@link ScheduledExecutorService} в одном scheduled потоке.
 */
public final class ScheduledSimulationRunner {

    private final StopConditionEvaluator stopCondition = new StopConditionEvaluator();

    public long runWithExecutionObserver(
            SimulationEngine engine,
            long maxTicks,
            long tickPeriodMillis,
            long reportEveryTicks,
            SimulationRunner.TickExecutionObserver observer
    ) {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(observer, "observer");
        if (maxTicks < 0) {
            throw new IllegalArgumentException("maxTicks must be non-negative");
        }
        if (tickPeriodMillis <= 0) {
            throw new IllegalArgumentException("tickPeriodMillis must be > 0");
        }
        if (reportEveryTicks < 0) {
            throw new IllegalArgumentException("reportEveryTicks must be non-negative");
        }
        if (maxTicks == 0) {
            return 0L;
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        CountDownLatch done = new CountDownLatch(1);
        AtomicLong executed = new AtomicLong(0L);
        AtomicReference<RuntimeException> failure = new AtomicReference<>();

        Runnable tickTask = () -> {
            try {
                TickExecution execution = engine.tickWithStats();
                long tick = execution.tickNumber();
                executed.set(tick);

                if (reportEveryTicks > 0 && tick % reportEveryTicks == 0) {
                    observer.onTickCompleted(execution, engine.context());
                }

                boolean mustStop = tick >= maxTicks
                        || stopCondition.shouldStop(engine.context().island(), engine.context().config().island().stopCondition());
                if (mustStop) {
                    done.countDown();
                }
            } catch (RuntimeException e) {
                failure.compareAndSet(null, e);
                done.countDown();
            }
        };

        try {
            scheduler.scheduleWithFixedDelay(tickTask, 0L, tickPeriodMillis, TimeUnit.MILLISECONDS);
            done.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            scheduler.shutdownNow();
        }

        RuntimeException e = failure.get();
        if (e != null) {
            throw e;
        }
        return executed.get();
    }
}
