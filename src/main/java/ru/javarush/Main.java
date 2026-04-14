package ru.javarush;

import ru.javarush.config.IslandConfigLoader;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.config.IslandSettings;
import ru.javarush.config.StopCondition;
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

    public static void main(String[] args) {
        CliOptions options = CliParser.parse(args);
        if (options.help()) {
            System.out.println(CliParser.usageText());
            return;
        }

        IslandSimulationConfig config = applyCliOverrides(loadConfig(options.configLocation()), options.stopConditionType());
        var settings = config.island();
        long tickDelayMillis = resolveTickDelayMillis(options, settings.tickDurationMillis());
        Random random = options.seed() != null ? new Random(options.seed()) : new Random();

        System.out.printf(
                "Island %d×%d, stop: %s, max ticks: %d, tick delay: %d ms, seed: %s%n",
                settings.width(),
                settings.height(),
                settings.stopCondition().type(),
                options.maxTicks(),
                tickDelayMillis,
                options.seed() != null ? options.seed() : "random");

        Island island = new IslandBuilder(random).build(config);
        printSnapshot("Start", island, null);

        var simulationContext = new SimulationContext(island, config, random);
        var engine = SimulationEngine.withDefaultPhases(simulationContext);
        long executed = new SimulationRunner().runWithExecutionObserver(
                engine,
                options.maxTicks(),
                tickDelayMillis,
                options.reportEveryTicks(),
                (execution, ctx) -> printSnapshot("Tick " + execution.tickNumber(), ctx.island(), execution));

        var stopEval = new StopConditionEvaluator();
        boolean stopMatched = stopEval.shouldStop(island, settings.stopCondition());

        printSnapshot("Done", island, null);
        System.out.printf("Executed ticks: %d, stop condition met: %b%n", executed, stopMatched);
    }

    static IslandSimulationConfig loadConfig(String configLocation) {
        IslandConfigLoader loader = new IslandConfigLoader();
        if (configLocation == null) {
            return loader.loadDefault();
        }
        if (configLocation.isBlank()) {
            throw new IllegalArgumentException("--config must not be empty (example: --config=config/island.yml)");
        }
        return loader.load(configLocation);
    }

    static IslandSimulationConfig applyCliOverrides(IslandSimulationConfig config, String stopOverride) {
        if (stopOverride == null) {
            return config;
        }
        var island = config.island();
        var overriddenIsland = new IslandSettings(
                island.width(),
                island.height(),
                island.tickDurationMillis(),
                island.initialAnimals(),
                new StopCondition(stopOverride),
                island.maxTicksWithoutFood(),
                island.plantGrowthChancePercent(),
                island.parallelMovementPlanning(),
                island.parallelPlantGrowthPlanning());
        return new IslandSimulationConfig(overriddenIsland, config.animals(), config.dietMatrix());
    }

    private static long resolveTickDelayMillis(CliOptions options, long configDefault) {
        long tickDelayMillis = options.tickDelayOverrideMillis() != null
                ? options.tickDelayOverrideMillis()
                : configDefault;
        if (tickDelayMillis < 0) {
            throw new IllegalArgumentException("tick delay must be >= 0 (use --tick-delay-ms=N or --no-delay)");
        }
        return tickDelayMillis;
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
