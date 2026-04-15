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
import ru.javarush.simulation.ScheduledSimulationRunner;
import ru.javarush.simulation.StopConditionEvaluator;
import ru.javarush.simulation.TickExecution;
import ru.javarush.view.CellGlyphResolver;
import ru.javarush.view.SpeciesGlyphTable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Точка входа: загрузка конфига, построение острова, цикл симуляции до стоп-условия или лимита тиков.
 */
public final class Main {
    private static final String ANSI_CLEAR_AND_HOME = "\u001B[2J\u001B[H";

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
                "Island %d×%d, stop: %s, max ticks: %d, tick delay: %d ms, seed: %s, mode: %s, map every: %d%n",
                settings.width(),
                settings.height(),
                settings.stopCondition().type(),
                options.maxTicks(),
                tickDelayMillis,
                options.seed() != null ? options.seed() : "random",
                options.scheduledMode() ? "scheduled" : "loop",
                options.renderMapEveryTicks());
        boolean liveUi = resolveLiveUiMode(options);

        SpeciesGlyphTable glyphTable = SpeciesGlyphTable.loadDefault();

        Island island = new IslandBuilder(random).build(config);
        if (liveUi) {
            renderLiveFrame("Start", island, null, glyphTable);
        } else {
            printSnapshot("Start", island, null);
            if (options.renderMapEveryTicks() > 0) {
                printMap("Start", island, glyphTable);
            }
        }

        var simulationContext = new SimulationContext(island, config, random);
        var engine = SimulationEngine.withDefaultPhases(simulationContext);
        long observerEveryTicks = liveUi ? 1L : effectiveObserverFrequency(options.reportEveryTicks(), options.renderMapEveryTicks());
        long executed;
        if (options.scheduledMode()) {
            long periodMillis = Math.max(1L, tickDelayMillis);
            if (tickDelayMillis == 0L) {
                System.out.println("Scheduled mode: tick delay 0ms adjusted to 1ms period.");
            }
            executed = new ScheduledSimulationRunner().runWithExecutionObserver(
                    engine,
                    options.maxTicks(),
                    periodMillis,
                    observerEveryTicks,
                    (execution, ctx) -> onTickObserved(execution, ctx.island(), options, glyphTable, liveUi));
        } else {
            executed = new SimulationRunner().runWithExecutionObserver(
                    engine,
                    options.maxTicks(),
                    tickDelayMillis,
                    observerEveryTicks,
                    (execution, ctx) -> onTickObserved(execution, ctx.island(), options, glyphTable, liveUi));
        }

        var stopEval = new StopConditionEvaluator();
        boolean stopMatched = stopEval.shouldStop(island, settings.stopCondition());

        if (liveUi) {
            renderLiveFrame("Done", island, null, glyphTable);
        } else {
            printSnapshot("Done", island, null);
            if (options.renderMapEveryTicks() > 0) {
                printMap("Done", island, glyphTable);
            }
        }
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

    private static long effectiveObserverFrequency(long reportEveryTicks, long renderMapEveryTicks) {
        if (reportEveryTicks > 0 && renderMapEveryTicks > 0) {
            return Math.min(reportEveryTicks, renderMapEveryTicks);
        }
        if (reportEveryTicks > 0) {
            return reportEveryTicks;
        }
        if (renderMapEveryTicks > 0) {
            return renderMapEveryTicks;
        }
        return 0L;
    }

    private static void onTickObserved(
            TickExecution execution,
            Island island,
            CliOptions options,
            SpeciesGlyphTable glyphTable,
            boolean liveUi) {
        long tick = execution.tickNumber();
        if (liveUi) {
            renderLiveFrame("Tick " + tick, island, execution, glyphTable);
            return;
        }
        if (options.reportEveryTicks() > 0 && tick % options.reportEveryTicks() == 0) {
            printSnapshot("Tick " + tick, island, execution);
        }
        if (options.renderMapEveryTicks() > 0 && tick % options.renderMapEveryTicks() == 0) {
            printMap("Map tick " + tick, island, glyphTable);
        }
    }

    private static void printSnapshot(String title, Island island, TickExecution execution) {
        System.out.println(snapshotLine(title, island, execution));
    }

    private static String snapshotLine(String title, Island island, TickExecution execution) {
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
        return "%s => total=%d, predators=%d, herbivores=%d, plants=%d, top=%s%s".formatted(
                title,
                island.totalCreatures(),
                predators,
                herbivores,
                plants,
                topSpecies,
                extras);
    }

    private static void printMap(String title, Island island, SpeciesGlyphTable glyphTable) {
        System.out.print(mapBlock(title, island, glyphTable));
    }

    private static String mapBlock(String title, Island island, SpeciesGlyphTable glyphTable) {
        StringBuilder out = new StringBuilder();
        out.append("%s map (%dx%d)%n".formatted(title, island.width(), island.height()));
        for (int row = 0; row < island.height(); row++) {
            StringBuilder line = new StringBuilder(island.width() * 4);
            for (int col = 0; col < island.width(); col++) {
                line.append(CellGlyphResolver.glyph(island.cell(row, col), glyphTable));
            }
            out.append(line).append(System.lineSeparator());
        }
        return out.toString();
    }

    private static boolean resolveLiveUiMode(CliOptions options) {
        if (options.uiMode() != UiMode.LIVE) {
            return false;
        }
        if (System.console() == null) {
            System.out.println("UI mode 'live' requested, but no interactive console detected. Falling back to 'stream'.");
            return false;
        }
        return true;
    }

    private static void renderLiveFrame(String title, Island island, TickExecution execution, SpeciesGlyphTable glyphTable) {
        StringBuilder frame = new StringBuilder();
        frame.append(ANSI_CLEAR_AND_HOME);
        frame.append(snapshotLine(title, island, execution)).append(System.lineSeparator());
        frame.append(System.lineSeparator());
        frame.append(mapBlock(title, island, glyphTable));
        System.out.print(frame);
        System.out.flush();
    }
}
