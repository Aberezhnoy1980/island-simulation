package ru.javarush;

import ru.javarush.config.IslandConfigLoader;

import java.util.Set;

/**
 * Парсер и валидатор аргументов командной строки.
 */
public final class CliParser {

    public static final long DEFAULT_MAX_TICKS = 500L;
    public static final long DEFAULT_REPORT_EVERY_TICKS = 50L;
    public static final Set<String> SUPPORTED_STOP_CONDITIONS = Set.of(
            "ALL_ANIMALS_DEAD",
            "NO_HERBIVORES",
            "NO_PREDATORS");

    private CliParser() {
    }

    public static CliOptions parse(String[] args) {
        if (shouldPrintHelp(args)) {
            return new CliOptions(true, false, DEFAULT_MAX_TICKS, DEFAULT_REPORT_EVERY_TICKS, 0L, null, null, null, null);
        }
        validateArgs(args);
        String stop = parseStopConditionType(args);
        if (stop != null && !SUPPORTED_STOP_CONDITIONS.contains(stop)) {
            throw new IllegalArgumentException("--stop must be one of " + SUPPORTED_STOP_CONDITIONS + ", got: " + stop);
        }
        long renderMapEvery = parseRenderMapEveryTicks(args);
        if (renderMapEvery < 0) {
            throw new IllegalArgumentException("--render-map-every must be >= 0");
        }
        return new CliOptions(
                false,
                parseScheduledMode(args),
                parseMaxTicks(args),
                parseReportEveryTicks(args),
                renderMapEvery,
                parseTickDelayOverrideMillis(args),
                parseSeed(args),
                parseConfigLocation(args),
                stop
        );
    }

    public static String usageText() {
        return """
                Usage: java -jar ... [OPTIONS] [MAX_TICKS]

                Options:
                  --ticks=N              Max simulation ticks (default 500)
                  --report-every=N       Print snapshot every N ticks (default 50)
                  --tick-delay-ms=N      Pause after each tick in ms (overrides island.tickDurationMillis in YAML)
                  --no-delay             Same as --tick-delay-ms=0
                  --scheduled            Run ticks via ScheduledExecutorService (single-thread scheduled mode)
                  --render-map-every=N   Print map snapshot every N ticks (0 disables)
                  --seed=N               Deterministic random seed for reproducible runs
                  --stop=TYPE            Override stop condition (ALL_ANIMALS_DEAD|NO_HERBIVORES|NO_PREDATORS)
                  --config=PATH          YAML file path or classpath resource (default: config/island.yml)
                  -h, --help             Show this message

                Examples:
                  --ticks=1000 --no-delay
                  --scheduled --tick-delay-ms=10 --report-every=1
                  --render-map-every=25 --report-every=50
                  --ticks=500 --seed=42 --no-delay
                  --stop=NO_HERBIVORES --report-every=1
                  --report-every=1 --tick-delay-ms=0
                  --config=config/island.yml --ticks=200
                  --config=/tmp/my-island.yml
                  --ticks=2000000 --report-every=1 --render-map-every=1 --tick-delay-ms=500
                """;
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
                || "--scheduled".equals(arg)
                || arg.startsWith("--ticks=")
                || arg.startsWith("--report-every=")
                || arg.startsWith("--render-map-every=")
                || arg.startsWith("--tick-delay-ms=")
                || arg.startsWith("--seed=")
                || arg.startsWith("--stop=")
                || arg.startsWith("--config=");
    }

    static long parseMaxTicks(String[] args) {
        for (String a : args) {
            if (a.startsWith("--ticks=")) {
                return Long.parseLong(a.substring("--ticks=".length()));
            }
        }
        for (String a : args) {
            if (!a.startsWith("-")) {
                return Long.parseLong(a);
            }
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

    static long parseRenderMapEveryTicks(String[] args) {
        for (String a : args) {
            if (a.startsWith("--render-map-every=")) {
                return Long.parseLong(a.substring("--render-map-every=".length()));
            }
        }
        return 0L;
    }

    static Long parseTickDelayOverrideMillis(String[] args) {
        Long explicit = null;
        for (String a : args) {
            if ("--no-delay".equals(a)) {
                explicit = 0L;
            } else if (a.startsWith("--tick-delay-ms=")) {
                explicit = Long.parseLong(a.substring("--tick-delay-ms=".length()));
            }
        }
        return explicit;
    }

    static Long parseSeed(String[] args) {
        Long seed = null;
        for (String a : args) {
            if (a.startsWith("--seed=")) {
                seed = Long.parseLong(a.substring("--seed=".length()));
            }
        }
        return seed;
    }

    static boolean parseScheduledMode(String[] args) {
        for (String a : args) {
            if ("--scheduled".equals(a)) {
                return true;
            }
        }
        return false;
    }

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
}
