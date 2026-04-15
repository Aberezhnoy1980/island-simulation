package ru.javarush.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandConfigLoaderTest {

    @Test
    void loadsDefaultIslandYamlFromClasspath() {
        IslandSimulationConfig cfg = new IslandConfigLoader().loadDefault();

        assertNotNull(cfg.island());
        assertEquals(100, cfg.island().width());
        assertEquals(20, cfg.island().height());
        assertEquals(500L, cfg.island().tickDurationMillis());
        assertEquals(5, cfg.island().maxTicksWithoutFood());
        assertEquals(30, cfg.island().plantGrowthChancePercent());
        assertEquals(false, cfg.island().parallelMovementPlanning());
        assertEquals(false, cfg.island().parallelPlantGrowthPlanning());
        assertFalse(cfg.island().effectiveParallelMovementPlanning());
        assertFalse(cfg.island().effectiveParallelPlantGrowthPlanning());
        assertEquals("ALL_ANIMALS_DEAD", cfg.island().stopCondition().type());

        assertNotNull(cfg.animals());
        assertTrue(cfg.animals().size() >= 10);
        assertNotNull(cfg.animals().get("wolf"));
        assertEquals("Волк", cfg.animals().get("wolf").name());

        assertNotNull(cfg.dietMatrix());
        assertTrue(cfg.dietMatrix().containsKey("wolf"));
    }

    @Test
    void loadReadsFromFileWhenPathExists(@TempDir Path tempDir) throws Exception {
        Path copy = tempDir.resolve("custom.yml");
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config/island.yml")) {
            Files.copy(Objects.requireNonNull(in), copy, StandardCopyOption.REPLACE_EXISTING);
        }
        IslandSimulationConfig cfg = new IslandConfigLoader().load(copy.toString());
        assertEquals(100, cfg.island().width());
    }

    @Test
    void loadFallsBackToClasspathWhenPathIsNotAFile() {
        IslandSimulationConfig cfg = new IslandConfigLoader().load("config/island.yml");
        assertEquals(100, cfg.island().width());
    }

    @Test
    void loadsDemoIslandYamlFromClasspath() {
        IslandSimulationConfig cfg = new IslandConfigLoader().load("config/demo-island.yml");
        assertEquals(24, cfg.island().width());
        assertEquals(8, cfg.island().height());
        assertEquals(5, cfg.animals().size());
    }
}
