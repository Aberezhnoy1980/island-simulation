package ru.javarush.config;

import java.util.Map;

/**
 * Параметры острова и стартовые популяции из секции {@code island}.
 */
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
    /**
     * Параллельное планирование движения.
     * <p>
     * Поле nullable для обратной совместимости со старыми YAML: если ключ не задан, значение будет {@code null},
     * что здесь трактуется как {@code false}.
     */
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
