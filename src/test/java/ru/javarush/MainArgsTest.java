package ru.javarush;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
