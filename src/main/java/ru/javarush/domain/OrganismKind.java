package ru.javarush.domain;

/**
 * Тип организма из конфигурации ({@code animals.*.type}).
 */
public enum OrganismKind {
    PREDATOR,
    HERBIVORE,
    PLANT;

    public static OrganismKind fromConfig(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("organism type must not be blank");
        }
        return valueOf(type.trim().toUpperCase());
    }
}
