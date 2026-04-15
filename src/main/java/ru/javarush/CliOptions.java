package ru.javarush;

/**
 * Нормализованные CLI-опции после валидации аргументов.
 */
public record CliOptions(
        boolean help,
        UiMode uiMode,
        boolean scheduledMode,
        long maxTicks,
        long reportEveryTicks,
        long renderMapEveryTicks,
        Long tickDelayOverrideMillis,
        Long seed,
        String configLocation,
        String stopConditionType
) {
}
