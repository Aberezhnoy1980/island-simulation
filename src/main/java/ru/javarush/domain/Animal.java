package ru.javarush.domain;

import ru.javarush.config.AnimalSettings;

/**
 * Животное (хищник или травоядный). Общее поведение появится в сервисах симуляции.
 */
public abstract class Animal extends Organism {

    private static final double KG_EPS = 1e-9;

    private double foodConsumedThisTick;

    protected Animal(String speciesId, AnimalSettings settings) {
        super(speciesId, settings);
    }

    /** Вызывается в начале фазы питания. */
    public void startFeedingRound() {
        foodConsumedThisTick = 0.0;
    }

    public double foodConsumedThisTick() {
        return foodConsumedThisTick;
    }

    public boolean canConsumePreyWeight(double preyWeightKg) {
        if (preyWeightKg <= 0) {
            return false;
        }
        return foodConsumedThisTick + preyWeightKg <= settings().maxFoodKg() + KG_EPS;
    }

    public void registerMeal(double preyWeightKg) {
        if (!canConsumePreyWeight(preyWeightKg)) {
            throw new IllegalStateException("Cannot consume " + preyWeightKg + " kg for " + speciesId());
        }
        foodConsumedThisTick += preyWeightKg;
    }
}
