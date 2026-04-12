package ru.javarush.domain;

import ru.javarush.config.AnimalSettings;

import java.util.Objects;

/**
 * Участник симуляции на клетке (животное или растение). Дальше сюда добавятся сытость, возраст и т. п.
 */
public abstract class Organism {

    private final String speciesId;
    private final AnimalSettings settings;

    protected Organism(String speciesId, AnimalSettings settings) {
        this.speciesId = Objects.requireNonNull(speciesId, "speciesId");
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public final String speciesId() {
        return speciesId;
    }

    public final AnimalSettings settings() {
        return settings;
    }

    public final OrganismKind kind() {
        return OrganismKind.fromConfig(settings.type());
    }
}
