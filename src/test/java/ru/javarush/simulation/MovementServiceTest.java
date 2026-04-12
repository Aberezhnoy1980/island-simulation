package ru.javarush.simulation;

import org.junit.jupiter.api.Test;
import ru.javarush.config.AnimalSettings;
import ru.javarush.domain.Herbivore;
import ru.javarush.domain.Island;
import ru.javarush.domain.Plant;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovementServiceTest {

    private final MovementService movement = new MovementService();

    /** Всегда выбирает направление с индексом {@code direction} (0=N, 1=E, 2=S, 3=W). */
    private static Random fixedDirection(int direction) {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                if (bound == 4) {
                    return direction;
                }
                return super.nextInt(bound);
            }
        };
    }

    @Test
    void rabbitMovesEastBySpeedSteps() {
        Island island = new Island(10, 10);
        var rabbit = new Herbivore(
                "rabbit",
                new AnimalSettings("Кролик", 2.0, 150, 3, 0.45, "HERBIVORE", null));
        island.cell(0, 0).add(rabbit);

        movement.relocateMobileOrganisms(island, fixedDirection(1));

        assertEquals(0, island.cell(0, 0).totalCreatures());
        assertEquals(1, island.cell(0, 3).totalCreatures());
        assertTrue(island.cell(0, 3).residentsView().contains(rabbit));
        assertEquals(1, island.totalCreatures());
    }

    @Test
    void plantDoesNotMove() {
        Island island = new Island(5, 5);
        var plant = new Plant(
                "plant",
                new AnimalSettings("Растения", 1.0, 200, 0, 0.0, "PLANT", null));
        island.cell(2, 2).add(plant);

        movement.relocateMobileOrganisms(island, fixedDirection(1));

        assertEquals(1, island.cell(2, 2).totalCreatures());
        assertTrue(island.cell(2, 2).residentsView().contains(plant));
    }

    @Test
    void zeroSpeedHerbivoreDoesNotMove() {
        Island island = new Island(5, 5);
        var caterpillar = new Herbivore(
                "caterpillar",
                new AnimalSettings("Гусеница", 0.01, 1000, 0, 0.0, "HERBIVORE", null));
        island.cell(1, 1).add(caterpillar);

        movement.relocateMobileOrganisms(island, fixedDirection(1));

        assertEquals(1, island.cell(1, 1).totalCreatures());
        assertTrue(island.cell(1, 1).residentsView().contains(caterpillar));
    }

    @Test
    void stopsAtNorthernBorder() {
        Island island = new Island(3, 3);
        var rabbit = new Herbivore(
                "rabbit",
                new AnimalSettings("Кролик", 2.0, 150, 5, 0.45, "HERBIVORE", null));
        island.cell(0, 1).add(rabbit);
        // always north -> cannot leave row 0
        movement.relocateMobileOrganisms(island, fixedDirection(0));

        assertEquals(1, island.cell(0, 1).totalCreatures());
        assertTrue(island.cell(0, 1).residentsView().contains(rabbit));
    }

    @Test
    void totalPopulationUnchangedWithManyCreatures() {
        Island island = new Island(20, 20);
        var rnd = new Random(777);
        for (int i = 0; i < 50; i++) {
            var h = new Herbivore(
                    "rabbit",
                    new AnimalSettings("Кролик", 2.0, 150, 2, 0.45, "HERBIVORE", null));
            island.cell(rnd.nextInt(20), rnd.nextInt(20)).add(h);
        }
        int before = island.totalCreatures();
        movement.relocateMobileOrganisms(island, new Random(888));
        assertEquals(before, island.totalCreatures());
    }
}
