package ru.javarush.simulation;

import org.junit.jupiter.api.Test;
import ru.javarush.config.AnimalSettings;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.config.IslandSettings;
import ru.javarush.config.StopCondition;
import ru.javarush.domain.Herbivore;
import ru.javarush.domain.Island;
import ru.javarush.domain.Plant;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeathServiceTest {

    private final DeathService death = new DeathService();
    private final FeedingService feeding = new FeedingService();

    private static IslandSimulationConfig configRabbitOnly(int maxTicksWithoutFood) {
        var animals = Map.of(
                "rabbit",
                new AnimalSettings("Кролик", 2.0, 150, 2, 0.45, "HERBIVORE"),
                "plant",
                new AnimalSettings("Растения", 0.2, 200, 0, 0.0, "PLANT"));
        var diet = Map.of("rabbit", Map.of("plant", 100));
        var island = new IslandSettings(
                3,
                3,
                500L,
                Map.of(),
                new StopCondition("NONE"),
                maxTicksWithoutFood);
        return new IslandSimulationConfig(island, animals, diet);
    }

    @Test
    void rabbitDiesAfterMaxTicksWithoutFood() {
        IslandSimulationConfig cfg = configRabbitOnly(2);
        Island island = new Island(3, 3);
        var rabbit = new Herbivore("rabbit", cfg.animals().get("rabbit"));
        island.cell(1, 1).add(rabbit);
        var rnd = new Random(1);

        feeding.feedAll(island, cfg, rnd);
        death.applyStarvation(island, cfg.island());
        assertEquals(1, island.totalCreatures());
        assertEquals(1, rabbit.ticksWithoutFood());

        feeding.feedAll(island, cfg, rnd);
        death.applyStarvation(island, cfg.island());
        assertEquals(0, island.totalCreatures());
    }

    @Test
    void eatingResetsHungerCounter() {
        IslandSimulationConfig cfg = configRabbitOnly(5);
        Island island = new Island(3, 3);
        var rabbit = new Herbivore("rabbit", cfg.animals().get("rabbit"));
        island.cell(0, 0).add(rabbit);
        island.cell(0, 0).add(new Plant("plant", cfg.animals().get("plant")));
        var rnd = FeedingServiceTest.alwaysWinRoll();

        feeding.feedAll(island, cfg, rnd);
        death.applyStarvation(island, cfg.island());
        assertEquals(0, rabbit.ticksWithoutFood());

        island.cell(0, 0).add(new Plant("plant", cfg.animals().get("plant")));
        feeding.feedAll(island, cfg, rnd);
        death.applyStarvation(island, cfg.island());
        assertEquals(0, rabbit.ticksWithoutFood());
        assertTrue(island.cell(0, 0).residentsView().contains(rabbit));
    }

    @Test
    void plantIgnoredByStarvation() {
        IslandSimulationConfig cfg = configRabbitOnly(1);
        Island island = new Island(2, 2);
        var plant = new Plant("plant", cfg.animals().get("plant"));
        island.cell(0, 0).add(plant);
        var rnd = new Random(3);

        for (int i = 0; i < 5; i++) {
            feeding.feedAll(island, cfg, rnd);
            death.applyStarvation(island, cfg.island());
        }
        assertEquals(1, island.totalCreatures());
        assertTrue(island.cell(0, 0).residentsView().contains(plant));
    }
}
