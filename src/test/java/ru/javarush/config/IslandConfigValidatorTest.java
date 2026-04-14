package ru.javarush.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandConfigValidatorTest {

    private final IslandConfigValidator validator = new IslandConfigValidator();

    private static AnimalSettings rabbit() {
        return new AnimalSettings("Кролик", 2.0, 150, 2, 0.45, "HERBIVORE", 20);
    }

    private static AnimalSettings plant() {
        return new AnimalSettings("Растения", 1.0, 200, 0, 0.0, "PLANT", null);
    }

    private static IslandSimulationConfig validMinimal() {
        IslandSettings island = new IslandSettings(
                3,
                3,
                100L,
                Map.of("rabbit", 2),
                new StopCondition("ALL_ANIMALS_DEAD"),
                5,
                30,
                null,
                null);
        return new IslandSimulationConfig(
                island,
                Map.of("rabbit", rabbit(), "plant", plant()),
                Map.of("rabbit", Map.of("plant", 100)));
    }

    @Test
    void acceptsValidConfig() {
        assertDoesNotThrow(() -> validator.validate(validMinimal()));
    }

    @Test
    void rejectsUnknownSpeciesInInitialAnimals() {
        IslandSimulationConfig cfg = new IslandSimulationConfig(
                new IslandSettings(3, 3, 0, Map.of("dragon", 1), new StopCondition("ALL_ANIMALS_DEAD"), 5, 30, null, null),
                Map.of("rabbit", rabbit(), "plant", plant()),
                Map.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(cfg));
        assertTrue(ex.getMessage().contains("unknown species"));
    }

    @Test
    void rejectsInvalidPercentsAndStopCondition() {
        AnimalSettings badRabbit = new AnimalSettings("Кролик", 2.0, 150, 2, 0.45, "HERBIVORE", 101);
        IslandSimulationConfig cfg = new IslandSimulationConfig(
                new IslandSettings(3, 3, 0, Map.of(), new StopCondition("SOMETHING_ELSE"), 5, -1, null, null),
                Map.of("rabbit", badRabbit, "plant", plant()),
                Map.of("rabbit", Map.of("plant", 150)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(cfg));
        String msg = ex.getMessage();
        assertTrue(msg.contains("stopCondition"));
        assertTrue(msg.contains("plantGrowthChancePercent"));
        assertTrue(msg.contains("reproductionChancePercent"));
        assertTrue(msg.contains("dietMatrix.rabbit.plant"));
    }

    @Test
    void rejectsInvalidAnimalType() {
        AnimalSettings invalidType = new AnimalSettings("X", 1, 1, 0, 0, "ALIEN", null);
        IslandSimulationConfig cfg = new IslandSimulationConfig(
                new IslandSettings(2, 2, 0, Map.of(), new StopCondition("ALL_ANIMALS_DEAD"), 1, 0, null, null),
                Map.of("x", invalidType),
                Map.of());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(cfg));
        assertTrue(ex.getMessage().contains("animals.x.type is invalid"));
    }
}
