package ru.javarush.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Параметры острова и стартовые популяции из секции {@code island}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IslandSettings(
        int width,
        int height,
        long tickDurationMillis,
        Map<String, Integer> initialAnimals,
        StopCondition stopCondition,
        /** Сколько полных тиков подряд без еды переносит животное; {@code null} — взять дефолт движка симуляции. */
        Integer maxTicksWithoutFood,
        /**
         * Вероятность появления одной новой особи растения на клетке за тик (если ниже лимита вида);
         * {@code null} — дефолт 25 в {@code PlantGrowthService} (пакет simulation).
         */
        Integer plantGrowthChancePercent,
        /**
         * Если true, план перемещений в фазе movement строится параллельно (применение плана всё равно последовательно).
         */
        Boolean parallelMovementPlanning,
        /**
         * Если true, план появления растений в фазе plantGrowth строится параллельно (применение — последовательно).
         * {@code null} — то же поведение, что и у {@link #parallelMovementPlanning}, чтобы старые конфиги без ключа
         * вели себя как раньше.
         */
        Boolean parallelPlantGrowthPlanning
) {
    /** Параллельное планирование движения; {@code null} трактуется как false. */
    public boolean effectiveParallelMovementPlanning() {
        return Boolean.TRUE.equals(parallelMovementPlanning());
    }

    /**
     * Параллельное планирование роста растений; при незаданном ключе в YAML совпадает с
     * {@link #effectiveParallelMovementPlanning()}.
     */
    public boolean effectiveParallelPlantGrowthPlanning() {
        if (parallelPlantGrowthPlanning() != null) {
            return Boolean.TRUE.equals(parallelPlantGrowthPlanning());
        }
        return effectiveParallelMovementPlanning();
    }
}
