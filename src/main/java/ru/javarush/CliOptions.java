package ru.javarush;

/**
 * Нормализованные CLI-опции после валидации аргументов.
 */
public record CliOptions(
        boolean help,
        long maxTicks,
        long reportEveryTicks,
        Long tickDelayOverrideMillis,
        Long seed,
        String configLocation,
        String stopConditionType
) {
}
