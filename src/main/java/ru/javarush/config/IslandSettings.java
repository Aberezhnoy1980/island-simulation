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
        Integer plantGrowthChancePercent
) {
}
