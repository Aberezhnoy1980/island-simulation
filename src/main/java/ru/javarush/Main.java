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
import java.util.Set;

/**
 * Точка входа: загрузка конфига, построение острова, цикл симуляции до стоп-условия или лимита тиков.
 */
public final class Main {

    private static final long DEFAULT_MAX_TICKS = 500L;
    private static final long DEFAULT_REPORT_EVERY_TICKS = 50L;
    private static final Set<String> SUPPORTED_STOP_CONDITIONS = Set.of(
            "ALL_ANIMALS_DEAD",
            "NO_HERBIVORES",
            "NO_PREDATORS");

    public static void main(String[] args) {
        if (shouldPrintHelp(args)) {
            printUsage();
            return;
        }
        validateArgs(args);

        IslandSimulationConfig config = applyCliOverrides(loadConfig(args), args);
        var settings = config.island();

        long maxTicks = parseMaxTicks(args);
        long reportEveryTicks = parseReportEveryTicks(args);
        long tickDelayMillis = parseTickDelayMillis(args, settings.tickDurationMillis());
        Long seed = parseSeed(args);
        if (tickDelayMillis < 0) {
            throw new IllegalArgumentException("tick delay must be >= 0 (use --tick-delay-ms=N or --no-delay)");
        }
        Random random = seed != null ? new Random(seed) : new Random();

        System.out.printf(
                "Island %d×%d, stop: %s, max ticks: %d, tick delay: %d ms, seed: %s%n",
                settings.width(),
                settings.height(),
                settings.stopCondition().type(),
                maxTicks,
                tickDelayMillis,
                seed != null ? seed : "random");

        Island island = new IslandBuilder(random).build(config);
        printSnapshot("Start", island, null);

        var simulationContext = new SimulationContext(island, config, random);
        var engine = SimulationEngine.withDefaultPhases(simulationContext);
        long executed = new SimulationRunner().runWithExecutionObserver(
                engine,
                maxTicks,
                tickDelayMillis,
                reportEveryTicks,
                (execution, ctx) -> printSnapshot("Tick " + execution.tickNumber(), ctx.island(), execution));

        var stopEval = new StopConditionEvaluator();
        boolean stopMatched = stopEval.shouldStop(island, settings.stopCondition());

        printSnapshot("Done", island, null);
        System.out.printf("Executed ticks: %d, stop condition met: %b%n", executed, stopMatched);
    }

    static IslandSimulationConfig loadConfig(String[] args) {
        String location = parseConfigLocation(args);
        if (location == null) {
            return new IslandConfigLoader().loadDefault();
        }
        if (location.isBlank()) {
            throw new IllegalArgumentException("--config must not be empty (example: --config=config/island.yml)");
        }
        return new IslandConfigLoader().load(location);
    }

    static IslandSimulationConfig applyCliOverrides(IslandSimulationConfig config, String[] args) {
        String stopOverride = parseStopConditionType(args);
        if (stopOverride == null) {
            return config;
        }
        if (!SUPPORTED_STOP_CONDITIONS.contains(stopOverride)) {
            throw new IllegalArgumentException(
                    "--stop must be one of " + SUPPORTED_STOP_CONDITIONS + ", got: " + stopOverride);
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

    static boolean shouldPrintHelp(String[] args) {
        for (String a : args) {
            if ("--help".equals(a) || "-h".equals(a)) {
                return true;
            }
        }
        return false;
    }

    static void validateArgs(String[] args) {
        int positionalCount = 0;
        for (String a : args) {
            if (a.startsWith("-")) {
                if (!isSupportedOption(a)) {
                    throw new IllegalArgumentException("Unknown option: " + a + " (use --help)");
                }
                continue;
            }
            positionalCount++;
            if (positionalCount > 1) {
                throw new IllegalArgumentException("Only one positional MAX_TICKS argument is allowed");
            }
            try {
                Long.parseLong(a);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid positional MAX_TICKS value: " + a, e);
            }
        }
    }

    private static boolean isSupportedOption(String arg) {
        return "--help".equals(arg)
                || "-h".equals(arg)
                || "--no-delay".equals(arg)
                || arg.startsWith("--ticks=")
                || arg.startsWith("--report-every=")
                || arg.startsWith("--tick-delay-ms=")
                || arg.startsWith("--seed=")
                || arg.startsWith("--stop=")
                || arg.startsWith("--config=");
    }

    static void printUsage() {
        System.out.println("""
                Usage: java -jar ... [OPTIONS] [MAX_TICKS]

                Options:
                  --ticks=N              Max simulation ticks (default 500)
                  --report-every=N       Print snapshot every N ticks (default 50)
                  --tick-delay-ms=N      Pause after each tick in ms (overrides island.tickDurationMillis in YAML)
                  --no-delay             Same as --tick-delay-ms=0
                  --seed=N               Deterministic random seed for reproducible runs
                  --stop=TYPE            Override stop condition (ALL_ANIMALS_DEAD|NO_HERBIVORES|NO_PREDATORS)
                  --config=PATH          YAML file path or classpath resource (default: config/island.yml)
                  -h, --help             Show this message

                Examples:
                  --ticks=1000 --no-delay
                  --ticks=500 --seed=42 --no-delay
                  --stop=NO_HERBIVORES --report-every=1
                  --report-every=1 --tick-delay-ms=0
                  --config=config/island.yml --ticks=200
                  --config=/tmp/my-island.yml
                """);
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

    /**
     * @param configDefault значение из YAML, если CLI не переопределяет паузу
     */
    static long parseTickDelayMillis(String[] args, long configDefault) {
        Long explicit = null;
        for (String a : args) {
            if ("--no-delay".equals(a)) {
                explicit = 0L;
            } else if (a.startsWith("--tick-delay-ms=")) {
                explicit = Long.parseLong(a.substring("--tick-delay-ms=".length()));
            }
        }
        return explicit != null ? explicit : configDefault;
    }

    /** {@code null} — сид не задан, генератор инициализируется случайно. */
    static Long parseSeed(String[] args) {
        Long seed = null;
        for (String a : args) {
            if (a.startsWith("--seed=")) {
                seed = Long.parseLong(a.substring("--seed=".length()));
            }
        }
        return seed;
    }

    /** {@code null} — stop condition не переопределяется через CLI. */
    static String parseStopConditionType(String[] args) {
        String stop = null;
        for (String a : args) {
            if (a.startsWith("--stop=")) {
                stop = a.substring("--stop=".length()).trim().toUpperCase();
            }
        }
        if (stop == null || stop.isBlank()) {
            return null;
        }
        return stop;
    }

    /** {@code null} — взять дефолтный classpath-ресурс {@link IslandConfigLoader#DEFAULT_CLASSPATH_RESOURCE}. */
    static String parseConfigLocation(String[] args) {
        for (String a : args) {
            if (a.startsWith("--config=")) {
                return a.substring("--config=".length());
            }
        }
        return null;
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
