package ru.javarush;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainArgsTest {

    @Test
    void parsesTicksFlagAndPositional() {
        assertEquals(100L, Main.parseMaxTicks(new String[] {"--ticks=100"}));
        assertEquals(42L, Main.parseMaxTicks(new String[] {"42"}));
    }

    @Test
    void defaultWhenNoArgs() {
        assertEquals(500L, Main.parseMaxTicks(new String[0]));
    }

    @Test
    void parsesReportEveryFlag() {
        assertEquals(25L, Main.parseReportEveryTicks(new String[] {"--report-every=25"}));
        assertEquals(50L, Main.parseReportEveryTicks(new String[0]));
    }

    @Test
    void parsesTickDelayFromYamlWhenNoFlags() {
        assertEquals(500L, Main.parseTickDelayMillis(new String[0], 500L));
    }

    @Test
    void noDelayOverridesYaml() {
        assertEquals(0L, Main.parseTickDelayMillis(new String[] {"--no-delay"}, 500L));
    }

    @Test
    void tickDelayMsOverridesYaml() {
        assertEquals(12L, Main.parseTickDelayMillis(new String[] {"--tick-delay-ms=12"}, 500L));
    }

    @Test
    void lastTickDelayFlagWins() {
        assertEquals(0L, Main.parseTickDelayMillis(
                new String[] {"--tick-delay-ms=100", "--no-delay"},
                500L));
        assertEquals(7L, Main.parseTickDelayMillis(
                new String[] {"--no-delay", "--tick-delay-ms=7"},
                500L));
    }

    @Test
    void classpathConfigLocation() {
        assertNull(Main.parseClasspathConfigLocation(new String[0]));
        assertEquals("config/island.yml", Main.parseClasspathConfigLocation(new String[] {"--config=config/island.yml"}));
    }

    @Test
    void helpFlag() {
        assertTrue(Main.shouldPrintHelp(new String[] {"--help"}));
        assertTrue(Main.shouldPrintHelp(new String[] {"-h"}));
        assertFalse(Main.shouldPrintHelp(new String[] {"--ticks=10"}));
    }

    @Test
    void loadConfigDefaultLoads() {
        assertNotNull(Main.loadConfig(new String[0]));
    }

    @Test
    void loadConfigBlankPathThrows() {
        assertThrows(IllegalArgumentException.class, () -> Main.loadConfig(new String[] {"--config="}));
    }
}
