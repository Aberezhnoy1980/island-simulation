package ru.javarush;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.javarush.config.StopCondition;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainArgsTest {

    @Test
    void parsesTicksFlagAndPositional() {
        assertEquals(100L, CliParser.parse(new String[] {"--ticks=100"}).maxTicks());
        assertEquals(42L, CliParser.parse(new String[] {"42"}).maxTicks());
    }

    @Test
    void defaultWhenNoArgs() {
        assertEquals(CliParser.DEFAULT_MAX_TICKS, CliParser.parse(new String[0]).maxTicks());
    }

    @Test
    void parsesReportEveryFlag() {
        assertEquals(25L, CliParser.parse(new String[] {"--report-every=25"}).reportEveryTicks());
        assertEquals(CliParser.DEFAULT_REPORT_EVERY_TICKS, CliParser.parse(new String[0]).reportEveryTicks());
    }

    @Test
    void tickDelayOverrideUnsetWhenNoFlags() {
        assertNull(CliParser.parse(new String[0]).tickDelayOverrideMillis());
    }

    @Test
    void noDelayOverridesYaml() {
        assertEquals(0L, CliParser.parse(new String[] {"--no-delay"}).tickDelayOverrideMillis());
    }

    @Test
    void tickDelayMsOverridesYaml() {
        assertEquals(12L, CliParser.parse(new String[] {"--tick-delay-ms=12"}).tickDelayOverrideMillis());
    }

    @Test
    void lastTickDelayFlagWins() {
        assertEquals(0L, CliParser.parse(new String[] {"--tick-delay-ms=100", "--no-delay"}).tickDelayOverrideMillis());
        assertEquals(7L, CliParser.parse(new String[] {"--no-delay", "--tick-delay-ms=7"}).tickDelayOverrideMillis());
    }

    @Test
    void parsesSeedFlag() {
        assertNull(CliParser.parse(new String[0]).seed());
        assertEquals(42L, CliParser.parse(new String[] {"--seed=42"}).seed());
    }

    @Test
    void lastSeedFlagWins() {
        assertEquals(7L, CliParser.parse(new String[] {"--seed=1", "--seed=7"}).seed());
    }

    @Test
    void parsesStopOverrideFlag() {
        assertNull(CliParser.parse(new String[0]).stopConditionType());
        assertEquals("NO_HERBIVORES", CliParser.parse(new String[] {"--stop=no_herbivores"}).stopConditionType());
    }

    @Test
    void configLocationFlag() {
        assertNull(CliParser.parse(new String[0]).configLocation());
        assertEquals("config/island.yml", CliParser.parse(new String[] {"--config=config/island.yml"}).configLocation());
    }

    @Test
    void loadConfigLoadsFromYamlFileOnDisk(@TempDir Path tempDir) throws Exception {
        Path copy = tempDir.resolve("island-copy.yml");
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("config/island.yml")) {
            Files.copy(Objects.requireNonNull(in), copy, StandardCopyOption.REPLACE_EXISTING);
        }
        assertNotNull(Main.loadConfig(copy.toString()));
    }

    @Test
    void helpFlag() {
        assertTrue(CliParser.parse(new String[] {"--help"}).help());
        assertTrue(CliParser.parse(new String[] {"-h"}).help());
        assertFalse(CliParser.parse(new String[] {"--ticks=10"}).help());
    }

    @Test
    void loadConfigDefaultLoads() {
        assertNotNull(Main.loadConfig(null));
    }

    @Test
    void loadConfigBlankPathThrows() {
        assertThrows(IllegalArgumentException.class, () -> Main.loadConfig(""));
    }

    @Test
    void validateArgsAcceptsKnownFlagsAndSinglePositional() {
        assertDoesNotThrow(() -> CliParser.parse(new String[] {
                "--ticks=100",
                "--report-every=10",
                "--tick-delay-ms=0",
                "--seed=42",
                "--stop=ALL_ANIMALS_DEAD",
                "--config=config/island.yml",
                "200"
        }));
    }

    @Test
    void validateArgsRejectsUnknownOption() {
        assertThrows(IllegalArgumentException.class, () -> CliParser.parse(new String[] {"--unknown=1"}));
    }

    @Test
    void validateArgsRejectsMoreThanOnePositionalArgument() {
        assertThrows(IllegalArgumentException.class, () -> CliParser.parse(new String[] {"100", "200"}));
    }

    @Test
    void applyCliOverridesReplacesStopCondition() {
        var cfg = Main.loadConfig(null);
        var overridden = Main.applyCliOverrides(cfg, "NO_PREDATORS");
        assertEquals(new StopCondition("NO_PREDATORS"), overridden.island().stopCondition());
    }

    @Test
    void applyCliOverridesRejectsUnsupportedStopCondition() {
        assertThrows(IllegalArgumentException.class, () -> CliParser.parse(new String[] {"--stop=NONE"}));
    }
}
