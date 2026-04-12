package ru.javarush.domain;

import ru.javarush.config.AnimalSettings;

public final class Herbivore extends Animal {

    public Herbivore(String speciesId, AnimalSettings settings) {
        super(speciesId, settings);
    }
}
