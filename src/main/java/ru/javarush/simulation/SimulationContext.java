package ru.javarush.simulation;

import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.domain.Island;

import java.util.Objects;
import java.util.Random;

/**
 * Состояние и окружение одного шага симуляции: остров, конфиг, генератор случайностей, счётчик завершённых тиков.
 */
public final class SimulationContext {

    private final Island island;
    private final IslandSimulationConfig config;
    private final Random random;
    private long completedTicks;

    public SimulationContext(Island island, IslandSimulationConfig config, Random random) {
        this.island = Objects.requireNonNull(island, "island");
        this.config = Objects.requireNonNull(config, "config");
        this.random = Objects.requireNonNull(random, "random");
    }

    public Island island() {
        return island;
    }

    public IslandSimulationConfig config() {
        return config;
    }

    public Random random() {
        return random;
    }

    /** Сколько полных тиков (все фазы подряд) уже выполнено. */
    public long completedTicks() {
        return completedTicks;
    }

    void advanceTick() {
        completedTicks++;
    }
}
