package ru.javarush.config;

/**
 * Характеристики одного вида из секции {@code animals}.
 */
public record AnimalSettings(
        String name,
        double weightKg,
        int maxPerLocation,
        int speed,
        double maxFoodKg,
        String type,
        /** Вероятность размножения за тик при паре сытных особей, 0–100; {@code null} — дефолт движка. */
        Integer reproductionChancePercent
) {
}
