package ru.javarush.domain;

import ru.javarush.config.AnimalSettings;

/**
 * Животное (хищник или травоядный). Общее поведение появится в сервисах симуляции.
 */
public abstract class Animal extends Organism {

    private static final double KG_EPS = 1e-9;

    private double foodConsumedThisTick;
    private int ticksWithoutFood;

    protected Animal(String speciesId, AnimalSettings settings) {
        super(speciesId, settings);
    }

    /** Сколько полных тиков подряд без приёма пищи (обновляется в фазе смерти после питания). */
    public int ticksWithoutFood() {
        return ticksWithoutFood;
    }

    /**
     * После фазы питания: если за тик что-то съели — сброс; иначе увеличиваем счётчик голода.
     */
    public void recordHungerAfterFeedingPhase() {
        if (foodConsumedThisTick > KG_EPS) {
            ticksWithoutFood = 0;
        } else {
            ticksWithoutFood++;
        }
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
