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
        StopCondition stopCondition
) {
}
