package ru.javarush.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals("ALL_ANIMALS_DEAD", cfg.island().stopCondition().type());

        assertNotNull(cfg.animals());
        assertTrue(cfg.animals().size() >= 10);
        assertNotNull(cfg.animals().get("wolf"));
        assertEquals("Волк", cfg.animals().get("wolf").name());

        assertNotNull(cfg.dietMatrix());
        assertTrue(cfg.dietMatrix().containsKey("wolf"));
    }
}
