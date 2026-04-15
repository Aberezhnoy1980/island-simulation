package ru.javarush.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Условие остановки симуляции (значения приходят из YAML).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StopCondition(String type) {
}
