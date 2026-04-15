package ru.javarush.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandSettingsTest {

    @Test
    void effectivePlantParallelFallsBackToMovementWhenUnset() {
        var s = new IslandSettings(1, 1, 0L, Map.of(), new StopCondition("NONE"), null, null, true, null);
        assertTrue(s.effectiveParallelPlantGrowthPlanning());
    }

    @Test
    void effectivePlantParallelExplicitOverridesMovement() {
        var s = new IslandSettings(1, 1, 0L, Map.of(), new StopCondition("NONE"), null, null, true, false);
        assertFalse(s.effectiveParallelPlantGrowthPlanning());
    }

    @Test
    void effectivePlantParallelExplicitTrueWhenMovementFalse() {
        var s = new IslandSettings(1, 1, 0L, Map.of(), new StopCondition("NONE"), null, null, false, true);
        assertTrue(s.effectiveParallelPlantGrowthPlanning());
    }

    @Test
    void effectiveMovementParallelNullMeansFalse() {
        var s = new IslandSettings(1, 1, 0L, Map.of(), new StopCondition("NONE"), null, null, null, null);
        assertFalse(s.effectiveParallelMovementPlanning());
    }
}
