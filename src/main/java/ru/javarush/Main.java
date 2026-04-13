package ru.javarush;

import ru.javarush.config.IslandConfigLoader;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.domain.Island;
import ru.javarush.domain.IslandBuilder;
import ru.javarush.simulation.SimulationContext;
import ru.javarush.simulation.SimulationEngine;
import ru.javarush.simulation.SimulationRunner;
import ru.javarush.simulation.StopConditionEvaluator;

import java.util.Random;

/**
 * Точка входа: загрузка конфига, построение острова, цикл симуляции до стоп-условия или лимита тиков.
 */
public final class Main {

    private static final long DEFAULT_MAX_TICKS = 500L;

    public static void main(String[] args) {
        IslandSimulationConfig config = new IslandConfigLoader().loadDefault();
        var settings = config.island();

        long maxTicks = parseMaxTicks(args);
        Random random = new Random();

        System.out.printf(
                "Island %d×%d, stop: %s, max ticks: %d%n",
                settings.width(),
                settings.height(),
                settings.stopCondition().type(),
                maxTicks);

        Island island = new IslandBuilder(random).build(config);
        System.out.printf("Start: total creatures %d%n", island.totalCreatures());

        var simulationContext = new SimulationContext(island, config, random);
        var engine = SimulationEngine.withDefaultPhases(simulationContext);
        long executed = new SimulationRunner().run(engine, maxTicks);

        var stopEval = new StopConditionEvaluator();
        boolean stopMatched = stopEval.shouldStop(island, settings.stopCondition());

        System.out.printf(
                "Done: ticks=%d, creatures=%d, stop condition met=%b%n",
                executed,
                island.totalCreatures(),
                stopMatched);
    }

    static long parseMaxTicks(String[] args) {
        for (String a : args) {
            if (a.startsWith("--ticks=")) {
                return Long.parseLong(a.substring("--ticks=".length()));
            }
        }
        if (args.length > 0 && !args[0].startsWith("-")) {
            return Long.parseLong(args[0]);
        }
        return DEFAULT_MAX_TICKS;
    }
}
