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

class ReproductionServiceTest {

    private final FeedingService feeding = new FeedingService();
    private final ReproductionService reproduction = new ReproductionService();

    private static IslandSimulationConfig rabbitAndPlants(int maxPerLocation, Integer reproChance) {
        // Ровно одна порция растения за тик на кролика (иначе второе съедалось бы как «крупная» жертва)
        var rabbit = new AnimalSettings("Кролик", 2.0, maxPerLocation, 2, 0.1, "HERBIVORE", reproChance);
        var plant = new AnimalSettings("Растения", 0.1, 200, 0, 0.0, "PLANT", null);
        var animals = Map.of("rabbit", rabbit, "plant", plant);
        var diet = Map.of("rabbit", Map.of("plant", 100));
        var island = new IslandSettings(4, 4, 500L, Map.of(), new StopCondition("NONE"), null, null, null, null);
        return new IslandSimulationConfig(island, animals, diet);
    }

    @Test
    void twoFedParentsAndRoomSpawnOffspring() {
        IslandSimulationConfig cfg = rabbitAndPlants(10, 100);
        var rabbitSettings = cfg.animals().get("rabbit");
        var plantSettings = cfg.animals().get("plant");

        Island island = new Island(4, 4);
        island.cell(0, 0).add(new Herbivore("rabbit", rabbitSettings));
        island.cell(0, 0).add(new Herbivore("rabbit", rabbitSettings));
        island.cell(0, 0).add(new Plant("plant", plantSettings));
        island.cell(0, 0).add(new Plant("plant", plantSettings));

        feeding.feedAll(island, cfg, FeedingServiceTest.alwaysWinRoll());
        reproduction.reproduce(island, cfg, new Random(42));

        assertEquals(3, island.cell(0, 0).countOf("rabbit"));
    }

    @Test
    void noSpawnIfOnlyOneParentAte() {
        IslandSimulationConfig cfg = rabbitAndPlants(10, 100);
        var rabbitSettings = cfg.animals().get("rabbit");
        var plantSettings = cfg.animals().get("plant");

        Island island = new Island(4, 4);
        island.cell(0, 0).add(new Herbivore("rabbit", rabbitSettings));
        island.cell(0, 0).add(new Herbivore("rabbit", rabbitSettings));
        island.cell(0, 0).add(new Plant("plant", plantSettings));

        feeding.feedAll(island, cfg, FeedingServiceTest.alwaysWinRoll());
        reproduction.reproduce(island, cfg, new Random(42));

        assertEquals(2, island.cell(0, 0).countOf("rabbit"));
    }

    @Test
    void noSpawnWhenAtMaxPerLocation() {
        IslandSimulationConfig cfg = rabbitAndPlants(2, 100);
        var rabbitSettings = cfg.animals().get("rabbit");
        var plantSettings = cfg.animals().get("plant");

        Island island = new Island(3, 3);
        island.cell(1, 1).add(new Herbivore("rabbit", rabbitSettings));
        island.cell(1, 1).add(new Herbivore("rabbit", rabbitSettings));
        island.cell(1, 1).add(new Plant("plant", plantSettings));
        island.cell(1, 1).add(new Plant("plant", plantSettings));

        feeding.feedAll(island, cfg, FeedingServiceTest.alwaysWinRoll());
        reproduction.reproduce(island, cfg, new Random(1));

        assertEquals(2, island.cell(1, 1).countOf("rabbit"));
    }

    @Test
    void zeroChanceNeverSpawns() {
        IslandSimulationConfig cfg = rabbitAndPlants(10, 0);
        var rabbitSettings = cfg.animals().get("rabbit");
        var plantSettings = cfg.animals().get("plant");

        Island island = new Island(3, 3);
        island.cell(0, 0).add(new Herbivore("rabbit", rabbitSettings));
        island.cell(0, 0).add(new Herbivore("rabbit", rabbitSettings));
        island.cell(0, 0).add(new Plant("plant", plantSettings));
        island.cell(0, 0).add(new Plant("plant", plantSettings));

        feeding.feedAll(island, cfg, FeedingServiceTest.alwaysWinRoll());
        reproduction.reproduce(island, cfg, new Random(1));

        assertEquals(2, island.cell(0, 0).countOf("rabbit"));
    }
}
