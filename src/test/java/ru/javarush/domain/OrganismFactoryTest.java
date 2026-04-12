package ru.javarush.domain;

import org.junit.jupiter.api.Test;
import ru.javarush.config.IslandConfigLoader;
import ru.javarush.config.IslandSimulationConfig;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrganismFactoryTest {

    @Test
    void createsPredatorHerbivoreAndPlant() {
        IslandSimulationConfig cfg = new IslandConfigLoader().loadDefault();
        OrganismFactory factory = new OrganismFactory(cfg);

        assertInstanceOf(Predator.class, factory.create("wolf"));
        assertInstanceOf(Herbivore.class, factory.create("rabbit"));
        assertInstanceOf(Plant.class, factory.create("plant"));
    }

    @Test
    void rejectsUnknownSpecies() {
        IslandSimulationConfig cfg = new IslandConfigLoader().loadDefault();
        OrganismFactory factory = new OrganismFactory(cfg);

        assertThrows(IllegalArgumentException.class, () -> factory.create("dragon"));
    }
}
