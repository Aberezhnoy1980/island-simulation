package ru.javarush.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Корневой объект конфигурации симуляции, соответствующий {@code island.yml}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IslandSimulationConfig(
        IslandSettings island,
        Map<String, AnimalSettings> animals,
        Map<String, Map<String, Integer>> dietMatrix
) {
}
