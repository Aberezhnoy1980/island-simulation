package ru.javarush.domain;

import ru.javarush.config.AnimalSettings;
import ru.javarush.config.IslandSimulationConfig;

import java.util.Objects;

/**
 * Создаёт конкретный класс организма по идентификатору вида и секции {@code animals} конфига.
 */
public final class OrganismFactory {

    private final IslandSimulationConfig config;

    public OrganismFactory(IslandSimulationConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public Organism create(String speciesId) {
        Objects.requireNonNull(speciesId, "speciesId");
        AnimalSettings settings = config.animals().get(speciesId);
        if (settings == null) {
            throw new IllegalArgumentException("Unknown species id: " + speciesId);
        }
        return switch (OrganismKind.fromConfig(settings.type())) {
            case PREDATOR -> new Predator(speciesId, settings);
            case HERBIVORE -> new Herbivore(speciesId, settings);
            case PLANT -> new Plant(speciesId, settings);
        };
    }
}
