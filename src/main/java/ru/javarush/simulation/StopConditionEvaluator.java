package ru.javarush.simulation;

import ru.javarush.config.StopCondition;
import ru.javarush.domain.Animal;
import ru.javarush.domain.Island;
import ru.javarush.domain.OrganismKind;
import ru.javarush.domain.Organism;

/**
 * Проверка условия остановки симуляции после тика.
 */
public final class StopConditionEvaluator {

    public boolean shouldStop(Island island, StopCondition stopCondition) {
        if (stopCondition == null || stopCondition.type() == null || stopCondition.type().isBlank()) {
            return false;
        }
        return switch (stopCondition.type().trim().toUpperCase()) {
            case "ALL_ANIMALS_DEAD" -> allAnimalsDead(island);
            case "NO_HERBIVORES" -> noAnimalsOfKind(island, OrganismKind.HERBIVORE);
            case "NO_PREDATORS" -> noAnimalsOfKind(island, OrganismKind.PREDATOR);
            default -> false;
        };
    }

    private static boolean allAnimalsDead(Island island) {
        for (int row = 0; row < island.height(); row++) {
            for (int col = 0; col < island.width(); col++) {
                for (Organism o : island.cell(row, col).residentsView()) {
                    if (o instanceof Animal) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean noAnimalsOfKind(Island island, OrganismKind kind) {
        for (int row = 0; row < island.height(); row++) {
            for (int col = 0; col < island.width(); col++) {
                for (Organism o : island.cell(row, col).residentsView()) {
                    if (o instanceof Animal animal && animal.kind() == kind) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
