package ru.javarush;

import ru.javarush.config.IslandConfigLoader;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.domain.Island;
import ru.javarush.domain.IslandBuilder;
import ru.javarush.domain.OrganismKind;
import ru.javarush.simulation.SimulationContext;
import ru.javarush.simulation.SimulationEngine;
import ru.javarush.simulation.SimulationRunner;
import ru.javarush.simulation.StopConditionEvaluator;
import ru.javarush.simulation.TickExecution;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Точка входа: загрузка конфига, построение острова, цикл симуляции до стоп-условия или лимита тиков.
 */
public final class Main {

    private static final long DEFAULT_MAX_TICKS = 500L;
    private static final long DEFAULT_REPORT_EVERY_TICKS = 50L;

    public static void main(String[] args) {
        IslandSimulationConfig config = new IslandConfigLoader().loadDefault();
        var settings = config.island();

        long maxTicks = parseMaxTicks(args);
        long reportEveryTicks = parseReportEveryTicks(args);
        Random random = new Random();

        System.out.printf(
                "Island %d×%d, stop: %s, max ticks: %d, tick delay: %d ms%n",
                settings.width(),
                settings.height(),
                settings.stopCondition().type(),
                maxTicks,
                settings.tickDurationMillis());

        Island island = new IslandBuilder(random).build(config);
        printSnapshot("Start", island, null);

        var simulationContext = new SimulationContext(island, config, random);
        var engine = SimulationEngine.withDefaultPhases(simulationContext);
        long executed = new SimulationRunner().runWithExecutionObserver(
                engine,
                maxTicks,
                settings.tickDurationMillis(),
                reportEveryTicks,
                (execution, ctx) -> printSnapshot("Tick " + execution.tickNumber(), ctx.island(), execution));

        var stopEval = new StopConditionEvaluator();
        boolean stopMatched = stopEval.shouldStop(island, settings.stopCondition());

        printSnapshot("Done", island, null);
        System.out.printf("Executed ticks: %d, stop condition met: %b%n", executed, stopMatched);
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

    static long parseReportEveryTicks(String[] args) {
        for (String a : args) {
            if (a.startsWith("--report-every=")) {
                return Long.parseLong(a.substring("--report-every=".length()));
            }
        }
        return DEFAULT_REPORT_EVERY_TICKS;
    }

    private static void printSnapshot(String title, Island island, TickExecution execution) {
        Map<OrganismKind, Long> byKind = island.totalPopulationByKind();
        long predators = byKind.getOrDefault(OrganismKind.PREDATOR, 0L);
        long herbivores = byKind.getOrDefault(OrganismKind.HERBIVORE, 0L);
        long plants = byKind.getOrDefault(OrganismKind.PLANT, 0L);
        List<String> topSpecies = island.totalPopulationBySpecies().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(e -> e.getKey() + "=" + e.getValue())
                .toList();
        String extras = "";
        if (execution != null) {
            int delta = execution.creaturesAfter() - execution.creaturesBefore();
            String phaseStats = execution.phaseDurationNanos().entrySet().stream()
                    .map(e -> e.getKey() + "=" + String.format("%.2fms", e.getValue() / 1_000_000.0))
                    .toList()
                    .toString();
            extras = ", delta=" + delta + ", phases=" + phaseStats;
        }
        System.out.printf(
                "%s => total=%d, predators=%d, herbivores=%d, plants=%d, top=%s%s%n",
                title,
                island.totalCreatures(),
                predators,
                herbivores,
                plants,
                topSpecies,
                extras);
    }
}
