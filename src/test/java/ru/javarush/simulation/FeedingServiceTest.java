package ru.javarush.simulation;

import org.junit.jupiter.api.Test;
import ru.javarush.config.AnimalSettings;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.config.IslandSettings;
import ru.javarush.config.StopCondition;
import ru.javarush.domain.Herbivore;
import ru.javarush.domain.Island;
import ru.javarush.domain.Plant;
import ru.javarush.domain.Predator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedingServiceTest {

    private final FeedingService feeding = new FeedingService();

    /** {@code nextInt(100)} всегда 0 — любой шанс {@code > 0} срабатывает. */
    private static Random alwaysWinRoll() {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                if (bound == 100) {
                    return 0;
                }
                return super.nextInt(bound);
            }
        };
    }

    /** {@code nextInt(100)} всегда 99 — шанс &lt; 100 не проходит для типичных значений из конфига. */
    private static Random alwaysLoseRoll() {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                if (bound == 100) {
                    return 99;
                }
                return super.nextInt(bound);
            }
        };
    }

    private static IslandSimulationConfig minimalConfig(
            Map<String, AnimalSettings> animals,
            Map<String, Map<String, Integer>> diet) {
        var island = new IslandSettings(
                5,
                5,
                500L,
                Map.of(),
                new StopCondition("NONE"));
        return new IslandSimulationConfig(island, animals, diet);
    }

    @Test
    void predatorEatsRabbitWhenHuntSucceeds() {
        var animals = Map.of(
                "wolf",
                new AnimalSettings("Волк", 50.0, 30, 3, 8.0, "PREDATOR"),
                "rabbit",
                new AnimalSettings("Кролик", 2.0, 150, 2, 0.45, "HERBIVORE"));
        var diet = Map.of("wolf", Map.of("rabbit", 100));
        IslandSimulationConfig cfg = minimalConfig(animals, diet);

        Island island = new Island(3, 3);
        var wolf = new Predator("wolf", animals.get("wolf"));
        var rabbit = new Herbivore("rabbit", animals.get("rabbit"));
        island.cell(1, 1).add(wolf);
        island.cell(1, 1).add(rabbit);

        feeding.feedAll(island, cfg, alwaysWinRoll());

        assertEquals(1, island.cell(1, 1).totalCreatures());
        assertTrue(island.cell(1, 1).residentsView().contains(wolf));
        assertEquals(2.0, wolf.foodConsumedThisTick(), 1e-6);
    }

    @Test
    void predatorDoesNotEatWhenRollFails() {
        var animals = Map.of(
                "wolf",
                new AnimalSettings("Волк", 50.0, 30, 3, 8.0, "PREDATOR"),
                "rabbit",
                new AnimalSettings("Кролик", 2.0, 150, 2, 0.45, "HERBIVORE"));
        var diet = Map.of("wolf", Map.of("rabbit", 60));
        IslandSimulationConfig cfg = minimalConfig(animals, diet);

        Island island = new Island(3, 3);
        var wolf = new Predator("wolf", animals.get("wolf"));
        var rabbit = new Herbivore("rabbit", animals.get("rabbit"));
        island.cell(1, 1).add(wolf);
        island.cell(1, 1).add(rabbit);

        feeding.feedAll(island, cfg, alwaysLoseRoll());

        assertEquals(2, island.cell(1, 1).totalCreatures());
        assertEquals(0.0, wolf.foodConsumedThisTick(), 1e-6);
    }

    @Test
    void herbivoreEatsPlant() {
        var animals = Map.of(
                "rabbit",
                new AnimalSettings("Кролик", 2.0, 150, 2, 0.45, "HERBIVORE"),
                "plant",
                new AnimalSettings("Растения", 0.2, 200, 0, 0.0, "PLANT"));
        var diet = Map.of("rabbit", Map.of("plant", 100));
        IslandSimulationConfig cfg = minimalConfig(animals, diet);

        Island island = new Island(3, 3);
        var rabbit = new Herbivore("rabbit", animals.get("rabbit"));
        var plant = new Plant("plant", animals.get("plant"));
        island.cell(0, 0).add(rabbit);
        island.cell(0, 0).add(plant);

        feeding.feedAll(island, cfg, alwaysWinRoll());

        assertEquals(1, island.cell(0, 0).totalCreatures());
        assertTrue(island.cell(0, 0).residentsView().contains(rabbit));
        assertEquals(0.2, rabbit.foodConsumedThisTick(), 1e-6);
    }

    @Test
    void predatorEatsTwoLightPreyWithinMaxFoodKg() {
        var animals = Map.of(
                "wolf",
                new AnimalSettings("Волк", 50.0, 30, 3, 8.0, "PREDATOR"),
                "mouse",
                new AnimalSettings("Мышь", 0.05, 500, 1, 0.01, "HERBIVORE"));
        var diet = Map.of("wolf", Map.of("mouse", 100));
        IslandSimulationConfig cfg = minimalConfig(animals, diet);

        Island island = new Island(2, 2);
        var wolf = new Predator("wolf", animals.get("wolf"));
        island.cell(0, 0).add(wolf);
        island.cell(0, 0).add(new Herbivore("mouse", animals.get("mouse")));
        island.cell(0, 0).add(new Herbivore("mouse", animals.get("mouse")));

        feeding.feedAll(island, cfg, alwaysWinRoll());

        assertEquals(1, island.cell(0, 0).totalCreatures());
        assertEquals(0.1, wolf.foodConsumedThisTick(), 1e-6);
    }

    @Test
    void skipsPreyTooHeavyForRemainingCapacity() {
        var animals = new HashMap<String, AnimalSettings>();
        animals.put(
                "wolf",
                new AnimalSettings("Волк", 50.0, 30, 3, 2.0, "PREDATOR"));
        animals.put("deer", new AnimalSettings("Олень", 300.0, 20, 4, 50.0, "HERBIVORE"));
        animals.put("mouse", new AnimalSettings("Мышь", 0.05, 500, 1, 0.01, "HERBIVORE"));
        var diet = Map.of(
                "wolf",
                Map.of(
                        "deer", 100,
                        "mouse", 100));
        IslandSimulationConfig cfg = minimalConfig(animals, diet);

        Island island = new Island(2, 2);
        var wolf = new Predator("wolf", animals.get("wolf"));
        island.cell(0, 0).add(wolf);
        island.cell(0, 0).add(new Herbivore("deer", animals.get("deer")));
        island.cell(0, 0).add(new Herbivore("mouse", animals.get("mouse")));

        feeding.feedAll(island, cfg, alwaysWinRoll());

        assertEquals(2, island.cell(0, 0).totalCreatures());
        assertEquals(0.05, wolf.foodConsumedThisTick(), 1e-6);
    }
}
