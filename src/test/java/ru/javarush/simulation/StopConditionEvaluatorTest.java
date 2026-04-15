package ru.javarush.simulation;

import org.junit.jupiter.api.Test;
import ru.javarush.config.StopCondition;
import ru.javarush.domain.Herbivore;
import ru.javarush.domain.Island;
import ru.javarush.domain.Plant;
import ru.javarush.domain.Predator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StopConditionEvaluatorTest {

    private final StopConditionEvaluator evaluator = new StopConditionEvaluator();

    @Test
    void allAnimalsDeadWhenOnlyPlants() {
        Island island = new Island(2, 2);
        var plantSettings = new ru.javarush.config.AnimalSettings("Р", 1, 200, 0, 0, "PLANT", null);
        island.cell(0, 0).add(new Plant("plant", plantSettings));
        assertTrue(evaluator.shouldStop(island, new StopCondition("ALL_ANIMALS_DEAD")));
    }

    @Test
    void notStoppedWhenAnimalPresent() {
        Island island = new Island(2, 2);
        var s = new ru.javarush.config.AnimalSettings("К", 1, 10, 1, 1, "HERBIVORE", null);
        island.cell(0, 0).add(new Herbivore("rabbit", s));
        assertFalse(evaluator.shouldStop(island, new StopCondition("ALL_ANIMALS_DEAD")));
    }

    @Test
    void noHerbivoresCondition() {
        Island island = new Island(2, 2);
        var predator = new ru.javarush.config.AnimalSettings("В", 50, 10, 1, 1, "PREDATOR", null);
        island.cell(0, 0).add(new Predator("wolf", predator));
        assertTrue(evaluator.shouldStop(island, new StopCondition("NO_HERBIVORES")));

        var herbivore = new ru.javarush.config.AnimalSettings("К", 1, 10, 1, 1, "HERBIVORE", null);
        island.cell(0, 1).add(new Herbivore("rabbit", herbivore));
        assertFalse(evaluator.shouldStop(island, new StopCondition("NO_HERBIVORES")));
    }

    @Test
    void noPredatorsCondition() {
        Island island = new Island(2, 2);
        var herbivore = new ru.javarush.config.AnimalSettings("К", 1, 10, 1, 1, "HERBIVORE", null);
        island.cell(0, 0).add(new Herbivore("rabbit", herbivore));
        assertTrue(evaluator.shouldStop(island, new StopCondition("NO_PREDATORS")));

        var predator = new ru.javarush.config.AnimalSettings("В", 50, 10, 1, 1, "PREDATOR", null);
        island.cell(1, 1).add(new Predator("wolf", predator));
        assertFalse(evaluator.shouldStop(island, new StopCondition("NO_PREDATORS")));
    }
}
